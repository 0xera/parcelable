plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
}

android {
    namespace = "com.zero.xera.parcelable.demo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.zero.xera.parcelable.demo"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "MIRROR_PACKAGE", "\"$namespace\"")
    }

    flavorDimensions += "type"

    productFlavors {
        create("default") {
            dimension = "type"
            applicationIdSuffix = ".default"
            buildConfigField("String", "MIRROR_PACKAGE", "\"$namespace.default\"")
        }

        create("first") {
            dimension = "type"
            applicationIdSuffix = ".first"
            buildConfigField("String", "MIRROR_PACKAGE", "\"$namespace.second\"")
        }
        create("second") {
            dimension = "type"
            applicationIdSuffix = ".second"
            buildConfigField("String", "MIRROR_PACKAGE", "\"$namespace.first\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {

    implementation(project(":parcelable:slice"))
    implementation(project(":parcelable:stream"))

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
}