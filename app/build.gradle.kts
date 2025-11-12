plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.guardiant.app"
    compileSdk {
        version = release(34)
    }

    defaultConfig {
        applicationId = "com.guardiant.app"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    // ============================================
    // UI & MATERIAL
    // ============================================
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // ============================================
    // NAVIGATION
    // ============================================
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // ============================================
    // FIREBASE
    // ============================================
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-functions-ktx")

    // ============================================
    // NETWORKING (Retrofit)
    // ============================================
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // ============================================
    // LIFECYCLE & VIEWMODEL
    // ============================================
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3")
    implementation("androidx.fragment:fragment-ktx:1.8.1")

    // ============================================
    // SENSORS & LOCATION (NUEVO)
    // ============================================
    // Para acelerómetro y sensores
    implementation("androidx.core:core:1.13.1")

    // Para GPS y location services
    implementation("com.google.android.gms:play-services-location:21.1.0")

    // ============================================
    // DEVICE ADMIN (NUEVO)
    // ============================================
    // Ya está incluido en Android framework, pero lo hacemos explícito
    implementation("androidx.core:core:1.13.1")

    // ============================================
    // RECYCLERVIEW (Para AlertsFragment)
    // ============================================
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // ============================================
    // CARDVIEW (Para AlertsAdapter)
    // ============================================
    implementation("androidx.cardview:cardview:1.0.0")

    // ============================================
    // COROUTINES (Para tareas asincrónicas)
    // ============================================
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // ============================================
    // TESTING
    // ============================================
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}