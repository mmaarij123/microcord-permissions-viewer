import java.util.Properties

plugins {
    id("com.android.application") version "9.2.1"
    id("dev.mantikafasi.manticord.plugin") version "1.0.0"
}

val pluginPackage = "dev.mantikafasi.manticord.permissionsviewer"

manticordPlugin {
    id.set("manticord.permissionsviewer")
    entrypoint.set("$pluginPackage.PermissionsViewerPlugin")
    description.set("Inspect server roles, member permissions, and channel overwrites.")
    manticordVersionMinimum.set(1)
    manticordVersionMaximumExclusive.set(2)
    usesResources.set(false)
    async()
}

android {
    namespace = pluginPackage
    compileSdk {
        version = release(36) { minorApiLevel = 1 }
    }
    defaultConfig {
        applicationId = pluginPackage
        minSdk = 23
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.1"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    val signingFile = rootProject.file("keystore.properties")
    if (signingFile.isFile) {
        val keys = Properties().apply { signingFile.inputStream().use(::load) }
        val releaseKey = signingConfigs.create("release") {
            storeFile = rootProject.file(keys.getProperty("storeFile"))
            storePassword = keys.getProperty("storePassword")
            keyAlias = keys.getProperty("keyAlias")
            keyPassword = keys.getProperty("keyPassword")
        }
        buildTypes.named("release") { signingConfig = releaseKey }
    }
}

dependencies {
    val localSdk = file("../manticordrebuild/build/plugin-sdk/manticord-plugin-api-1.0.6.jar")
    val sdk = if (localSdk.isFile) files(localSdk) else "dev.mantikafasi.manticord:plugin-api:1.0.6"
    compileOnly(sdk)
    compileOnly("io.reactivex:rxjava:1.3.8")
    compileOnly("androidx.core:core:1.16.0") { isTransitive = false }
}
