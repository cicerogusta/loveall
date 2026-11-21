import java.net.URI
import java.util.zip.ZipFile

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
val uploadStoreFile = providers.environmentVariable("LOVEALL_UPLOAD_STORE_FILE").orNull
val uploadStorePassword = providers.environmentVariable("LOVEALL_UPLOAD_STORE_PASSWORD").orNull
val uploadKeyAlias = providers.environmentVariable("LOVEALL_UPLOAD_KEY_ALIAS").orNull
val uploadKeyPassword = providers.environmentVariable("LOVEALL_UPLOAD_KEY_PASSWORD").orNull
val uploadSigning = listOf(uploadStoreFile, uploadStorePassword, uploadKeyAlias, uploadKeyPassword)
require(uploadSigning.all { it.isNullOrBlank() } || uploadSigning.all { !it.isNullOrBlank() }) {
    "Configure all four LOVEALL_UPLOAD_* signing environment variables together"
}
val hasUploadSigning = uploadSigning.all { !it.isNullOrBlank() }
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
        versionCode = providers.gradleProperty("loveall.versionCode").orElse("1").get().toInt().also {
            require(it > 0) { "loveall.versionCode must be positive" }
        }
        versionName = providers.gradleProperty("loveall.versionName").orElse("1.0.0").get()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["admobAppId"] = testAdmobAppId
        buildConfigField("boolean", "ADS_ENABLED", adsEnabled.toString())
        buildConfigField("boolean", "PLAY_BILLING_ENABLED", billingEnabled.toString())
    }
    signingConfigs {
        if (hasUploadSigning) {
            create("upload") {
                storeFile = rootProject.file(requireNotNull(uploadStoreFile))
                storePassword = uploadStorePassword
                keyAlias = uploadKeyAlias
                keyPassword = uploadKeyPassword
            }
        }
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
            if (hasUploadSigning) signingConfig = signingConfigs.getByName("upload")
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

val validateUploadSigning = tasks.register("validateUploadSigning") {
    group = "verification"
    description = "Rejects Play publication without the configured upload key"
    doLast {
        check(hasUploadSigning) { "Use scripts/Build-PlayRelease.ps1 to load the protected upload key" }
        check(rootProject.file(requireNotNull(uploadStoreFile)).isFile) { "Upload keystore file not found" }
    }
}
tasks.matching { it.name == "bundleRelease" }.configureEach { mustRunAfter(validateUploadSigning) }
tasks.register("preparePlayRelease") {
    group = "publishing"
    description = "Builds a signed, minified Android App Bundle for Play testing"
    dependsOn(validateUploadSigning, "bundleRelease")
    doLast {
        val bundle = layout.buildDirectory.file("outputs/bundle/release/app-release.aab").get().asFile
        ZipFile(bundle).use { archive ->
            check(archive.entries().asSequence().any {
                it.name.startsWith("META-INF/") && it.name.endsWith(".RSA")
            }) { "The release bundle has no upload signature" }
        }
    }
}

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
