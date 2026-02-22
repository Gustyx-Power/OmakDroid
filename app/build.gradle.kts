plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "id.xms.omakdroid"
    compileSdk = 36

    defaultConfig {
        applicationId = "id.xms.omakdroid"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core:system"))
    implementation(project(":core:ui"))
    implementation(project(":core:common"))
    implementation(project(":feature:installer"))
    implementation(project(":feature:desktop"))
}
