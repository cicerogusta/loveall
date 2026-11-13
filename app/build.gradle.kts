import java.net.URI

plugins {
    jacoco
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

jacoco { toolVersion = "0.8.14" }

val businessClasses = fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
    include("com/loveall/app/domain/validation/Rules.class", "com/loveall/app/domain/model/Registration*.class", "com/loveall/app/domain/model/ProfileEdit*.class")
}
val businessExecutionData = layout.buildDirectory.file("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
tasks.register("requireBusinessCoverageData") {
    dependsOn("testDebugUnitTest")
    doLast {
        check(businessExecutionData.get().asFile.isFile) { "Unit test coverage execution data is missing" }
        check(!businessClasses.isEmpty) { "Business classes were not found for coverage verification" }
    }
}
tasks.register<JacocoReport>("businessCoverage") {
    dependsOn("requireBusinessCoverageData")
    classDirectories.setFrom(businessClasses)
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(businessExecutionData)
    reports { xml.required.set(true); html.required.set(true) }
}
tasks.register<JacocoCoverageVerification>("verifyBusinessCoverage") {
    dependsOn("businessCoverage")
    classDirectories.setFrom(businessClasses)
    executionData.setFrom(businessExecutionData)
    violationRules { rule { limit { counter = "LINE"; minimum = "0.80".toBigDecimal() } } }
}

val configuredApiUrl = providers.gradleProperty("loveall.apiBaseUrl").orNull
val adsEnabled = providers.gradleProperty("loveall.adsEnabled").orElse("false").get().toBooleanStrict()
val billingEnabled = providers.gradleProperty("loveall.playBillingEnabled").orElse("false").get().toBooleanStrict()
val admobAppId = providers.gradleProperty("loveall.admobAppId").orElse("").get()
val admobBannerId = providers.gradleProperty("loveall.admobBannerId").orElse("").get()
val testAdmobAppId = "ca-app-pub-3940256099942544~3347511713"
val testBannerId = "ca-app-pub-3940256099942544/9214589741"
val debugApiUrl = configuredApiUrl ?: "http://10.0.2.2:3001/api/"
fun quoted(value: String) = "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""
fun validateApiUrl(value: String) {
    val uri = URI(value)
    require(uri.scheme in listOf("http", "https") && uri.host != null &&
        uri.userInfo == null && uri.query == null && uri.fragment == null && value.endsWith("/")) {
        "loveall.apiBaseUrl must be an absolute HTTP(S) URL ending in /, without credentials, query or fragment"
    }
}
validateApiUrl(debugApiUrl)

android {
    namespace = "com.loveall.app"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.loveall.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["admobAppId"] = testAdmobAppId
        buildConfigField("boolean", "ADS_ENABLED", adsEnabled.toString())
        buildConfigField("boolean", "PLAY_BILLING_ENABLED", billingEnabled.toString())
    }
    buildTypes {
        debug {
            manifestPlaceholders["admobAppId"] = admobAppId.ifBlank { testAdmobAppId }
            applicationIdSuffix = ".native.debug"
            versionNameSuffix = "-migration"
            buildConfigField("String", "API_BASE_URL", quoted(debugApiUrl))
            buildConfigField("boolean", "EXTERNAL_CHECKOUT_ENABLED", "false")
            buildConfigField("String", "ADMOB_APP_ID", quoted(admobAppId.ifBlank { testAdmobAppId }))
            buildConfigField("String", "ADMOB_BANNER_ID", quoted(testBannerId))
        }
        release {
            isMinifyEnabled = true
            manifestPlaceholders["admobAppId"] = if (adsEnabled) admobAppId else testAdmobAppId
            buildConfigField("String", "ADMOB_APP_ID", quoted(admobAppId))
            buildConfigField("String", "ADMOB_BANNER_ID", quoted(admobBannerId))
            buildConfigField("String", "API_BASE_URL", quoted(configuredApiUrl ?: ""))
            buildConfigField("boolean", "EXTERNAL_CHECKOUT_ENABLED", "false")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    buildFeatures { compose = true; buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
kotlin { jvmToolchain(17) }

// A release must never silently use the emulator or an invented production host.
tasks.matching { it.name == "preReleaseBuild" }.configureEach {
    doFirst {
        if (adsEnabled) {
            require(Regex("ca-app-pub-[0-9]{16}~[0-9]{10}").matches(admobAppId) && !admobAppId.startsWith("ca-app-pub-3940256099942544")) { "Release ads require a real loveall.admobAppId" }
            require(Regex("ca-app-pub-[0-9]{16}/[0-9]{10}").matches(admobBannerId) && !admobBannerId.startsWith("ca-app-pub-3940256099942544")) { "Release ads require a real loveall.admobBannerId" }
        }
        require(!billingEnabled || adsEnabled) { "An ad-free subscription requires ads to be enabled for free accounts" }
        require(configuredApiUrl != null) { "Release requires -Ploveall.apiBaseUrl=https://YOUR_HOST/api/" }
        validateApiUrl(configuredApiUrl)
        require(URI(configuredApiUrl).scheme == "https") { "Release API must use HTTPS" }
    }
}

dependencies {
    implementation(libs.play.billing)
    implementation(libs.mobile.ads)
    implementation(libs.ump)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.preview)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.coroutines.android)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.navigation.compose)
    implementation(libs.coil.compose)
    implementation(libs.exifinterface)
    implementation(libs.compose.icons)
    debugImplementation(libs.compose.tooling)
    debugImplementation(libs.compose.test.manifest)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockwebserver)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test)
    androidTestImplementation(libs.androidx.test)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.mockwebserver)
    androidTestImplementation(libs.espresso.core)
}
