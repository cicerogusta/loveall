package com.loveall.app.data.repository

import com.loveall.app.data.apiCall
import com.loveall.app.data.remote.api.BillingApi
import com.loveall.app.data.remote.dto.EntitlementDto
import com.loveall.app.data.remote.dto.VerifyPurchaseRequest
import com.loveall.app.domain.model.*
import com.loveall.app.domain.repository.BillingRepository

class ApiBillingRepository(private val api: BillingApi) : BillingRepository {
    override suspend fun configuration() = apiCall {
        api.configuration().let {
            require(
                !it.enabled ||
                    (!it.productIds.isNullOrEmpty() &&
                        it.accountId?.matches(Regex(ACCOUNT_ID_PATTERN)) == true)
            )
            BillingConfiguration(it.enabled, it.productIds.orEmpty(), it.accountId)
        }
    }

    override suspend fun entitlement() = apiCall { api.entitlement().toDomain() }

    override suspend fun verify(purchase: StorePurchase) = apiCall {
        api.verify(VerifyPurchaseRequest(purchase.productId, purchase.token)).toDomain()
    }

    private fun EntitlementDto.toDomain(): AdFreeEntitlement {
        val active = requireNotNull(adFree)
        require(!active || runCatching { java.time.Instant.parse(expiresAt) }.isSuccess)
        return AdFreeEntitlement(active, expiresAt)
    }

    private companion object {
        const val ACCOUNT_ID_PATTERN = "[0-9a-f]{64}"
    }
}
