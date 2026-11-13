package com.loveall.app.platform.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.loveall.app.domain.error.AuthError
import com.loveall.app.domain.error.AuthException
import com.loveall.app.domain.model.*
import com.loveall.app.domain.repository.SubscriptionStore
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class GoogleSubscriptionStore(context: Context) : SubscriptionStore {
    private var activity = WeakReference<Activity>(null)
    private val events = MutableSharedFlow<StoreUpdate>(extraBufferCapacity = 16)
    override val updates = events.asSharedFlow()
    private val cached = ConcurrentHashMap<String, CachedSubscriptionOffer>()
    private val connection = Mutex()
    private val client =
        BillingClient.newBuilder(context.applicationContext)
            .setListener { result, purchases ->
                when (result.responseCode) {
                    BillingClient.BillingResponseCode.OK ->
                        events.tryEmit(StoreUpdate(purchases.orEmpty().mapNotNull(::toPurchase)))
                    BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED ->
                        events.tryEmit(StoreUpdate(restoreRequired = true))
                    BillingClient.BillingResponseCode.USER_CANCELED ->
                        events.tryEmit(StoreUpdate(cancelled = true))
                    else -> events.tryEmit(StoreUpdate(error = AuthError.SERVICE))
                }
            }
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
            )
            .enableAutoServiceReconnection()
            .build()

    fun attach(host: Activity) {
        activity = WeakReference(host)
    }

    fun detach(host: Activity) {
        if (activity.get() === host) activity.clear()
    }

    private suspend fun connect() = connection.withLock {
        if (client.isReady) return@withLock
        suspendCancellableCoroutine<Unit> { waiting ->
            client.startConnection(
                object : BillingClientStateListener {
                    override fun onBillingSetupFinished(result: BillingResult) {
                        if (!waiting.isActive) return
                        if (result.responseCode == BillingClient.BillingResponseCode.OK)
                            waiting.resume(Unit)
                        else waiting.resumeWithException(AuthException(AuthError.SERVICE))
                    }

                    override fun onBillingServiceDisconnected() {
                        if (waiting.isActive)
                            waiting.resumeWithException(AuthException(AuthError.SERVICE))
                    }
                }
            )
        }
    }

    override suspend fun offers(productIds: List<String>): List<SubscriptionOffer> = sdkCall {
        connect()
        if (productIds.isEmpty()) return@sdkCall emptyList()
        val query =
            QueryProductDetailsParams.newBuilder()
                .setProductList(
                    productIds.map {
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(it)
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()
                    }
                )
                .build()
        val details =
            suspendCancellableCoroutine<List<ProductDetails>> { waiting ->
                client.queryProductDetailsAsync(query) { result, response ->
                    if (!waiting.isActive) return@queryProductDetailsAsync
                    if (result.responseCode == BillingClient.BillingResponseCode.OK)
                        waiting.resume(response.productDetailsList)
                    else waiting.resumeWithException(AuthException(AuthError.SERVICE))
                }
            }
        cached.clear()
        details.flatMap(SubscriptionOfferMapper::standardOffers).map { (offer, native) ->
            cached[offer.id] = native
            offer
        }
    }

    override suspend fun restore(): List<StorePurchase> = sdkCall {
        connect()
        suspendCancellableCoroutine { waiting ->
            client.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            ) { result, purchases ->
                if (!waiting.isActive) return@queryPurchasesAsync
                if (result.responseCode == BillingClient.BillingResponseCode.OK)
                    waiting.resume(purchases.mapNotNull(::toPurchase))
                else waiting.resumeWithException(AuthException(AuthError.SERVICE))
            }
        }
    }

    override suspend fun purchase(offerId: String, accountId: String) = sdkCall {
        connect()
        val host =
            activity.get()?.takeUnless { it.isFinishing || it.isDestroyed }
                ?: throw AuthException(AuthError.SERVICE)
        val selected = cached[offerId] ?: throw AuthException(AuthError.VALIDATION)
        val params =
            BillingFlowParams.newBuilder()
                .setObfuscatedAccountId(accountId)
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(selected.details)
                            .setOfferToken(selected.token)
                            .build()
                    )
                )
                .build()
        val result = client.launchBillingFlow(host, params)
        if (result.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            events.tryEmit(StoreUpdate(cancelled = true))
            return@sdkCall
        }
        if (result.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            events.tryEmit(StoreUpdate(restoreRequired = true))
            return@sdkCall
        }
        if (result.responseCode != BillingClient.BillingResponseCode.OK)
            throw AuthException(AuthError.SERVICE)
    }

    private suspend fun <T> sdkCall(work: suspend () -> T): T =
        try {
            withTimeout(SDK_TIMEOUT_MS) { withContext(Dispatchers.Main.immediate) { work() } }
        } catch (_: TimeoutCancellationException) {
            throw AuthException(AuthError.NETWORK)
        }

    private fun toPurchase(purchase: Purchase): StorePurchase? {
        if (
            purchase.purchaseState != Purchase.PurchaseState.PURCHASED &&
                purchase.purchaseState != Purchase.PurchaseState.PENDING
        )
            return null
        val product = purchase.products.singleOrNull() ?: return null
        return StorePurchase(
            product,
            purchase.purchaseToken,
            purchase.purchaseState == Purchase.PurchaseState.PENDING,
        )
    }
}

private const val SDK_TIMEOUT_MS = 20_000L
