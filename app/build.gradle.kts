plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "tech.estacionkus.camerastream"
    compileSdk = 35

    defaultConfig {
        applicationId = "tech.estacionkus.camerastream"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "1.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk { abiFilters += listOf("arm64-v8a", "armeabi-v7a") }
    }

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_FILE") ?: "keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "camerastream123"
            keyAlias = System.getenv("KEY_ALIAS") ?: "camerastream"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "camerastream123"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true; buildConfig = true }
    packaging {
        resources { excludes += setOf("/META-INF/{AL2.0,LGPL2.1}", "META-INF/INDEX.LIST", "META-INF/DEPENDENCIES") }
        jniLibs { useLegacyPackaging = true }
    }
    lint { abortOnError = false; checkReleaseBuilds = false }
}

dependencies {
    // Compose BOM — verified 2026.03.00
    val composeBom = platform("androidx.compose:compose-bom:2026.03.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.navigation:navigation-compose:2.8.9")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Hilt — verified 2.55 + androidx 1.3.0
    implementation("com.google.dagger:hilt-android:2.55")
    ksp("com.google.dagger:hilt-android-compiler:2.55")
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")

    // CameraX
    val cameraXVersion = "1.4.2"
    implementation("androidx.camera:camera-core:$cameraXVersion")
    implementation("androidx.camera:camera-camera2:$cameraXVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraXVersion")
    implementation("androidx.camera:camera-view:$cameraXVersion")
    implementation("androidx.camera:camera-extensions:$cameraXVersion")

    // NodeMedia RTMP — local AAR downloaded by CI into app/libs
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar", "*.jar"))))

    // SRT — verified 1.9.1 on Maven Central
    implementation("io.github.thibaultbee.srtdroid:srtdroid-core:1.9.1")
    implementation("io.github.thibaultbee.srtdroid:srtdroid-ktx:1.9.1")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.3")

    // Supabase — verified BOM 3.1.4
    val supabaseVersion = "3.1.4"
    implementation(platform("io.github.jan-tennert.supabase:bom:$supabaseVersion"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt")
    implementation("io.ktor:ktor-client-okhttp:3.1.2")

    // Coil — verified 3.3.0
    implementation("io.coil-kt.coil3:coil-compose:3.3.0")
    implementation("io.coil-kt.coil3:coil-gif:3.3.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0")

    // OkHttp WebSocket (chat)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Serialization + Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")

    // ZXing QR
    implementation("com.google.zxing:core:3.5.3")

    // Splash + WorkManager
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.work:work-runtime-ktx:2.10.0")

    // Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.37.0")
}
