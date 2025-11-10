import com.android.build.api.variant.ApplicationAndroidComponentsExtension

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.promisclamping"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.promisclamping"
        minSdk = 21
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        buildConfig = true   // 👈 add this line
        compose = true
    }

    flavorDimensions += "environment"

    productFlavors {
        create("dev") {
            dimension = "environment"
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080/api/\"")
            buildConfigField("String", "BUCKET_NAME", "\"promis-dev-bucket\"")
            buildConfigField("String", "BASE_URL_KOMPAUN", "\"http://10.0.2.2:8091/api/\"")
            buildConfigField("String", "BASE_URL_UPLOAD", "\"http://10.0.2.2:8093/api/\"")
        }
        create("staging") {
            dimension = "environment"
            buildConfigField("String", "BASE_URL", "\"https://staging.promis.my/api/\"")
            buildConfigField("String", "BUCKET_NAME", "\"promis-staging-bucket\"")
            buildConfigField("String", "BASE_URL_KOMPAUN", "\"http://10.0.2.2:8091/api/\"")
            buildConfigField("String", "BASE_URL_UPLOAD", "\"http://10.0.2.2:8093/api/\"")
        }
        create("prod") {
            dimension = "environment"
            buildConfigField("String", "BASE_URL", "\"https://api.promis.my/api/\"")
            buildConfigField("String", "BUCKET_NAME", "\"promis-prod-bucket\"")
            buildConfigField("String", "BASE_URL_KOMPAUN", "\"http://10.0.2.2:8091/api/\"")
            buildConfigField("String", "BASE_URL_UPLOAD", "\"http://10.0.2.2:8093/api/\"")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}