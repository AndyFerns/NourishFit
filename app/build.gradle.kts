plugins {
    // These IDs now use the explicit, compatible versions from the project-level file.
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("androidx.room")
}

android {
    namespace = "com.example.nourishfit"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.nourishfit"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {  }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
//    // This reads the key from local.properties and makes it available
//    buildTypes.all {
//        buildConfigField("String", "GEMINI_API_KEY", "\"${project.findProperty("GEMINI_API_KEY")}\"")
//    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    val room_version = "2.6.1"

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.navigation.compose)

    // Room Database
    implementation("androidx.room:room-runtime:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    // SQLite support
    implementation("androidx.sqlite:sqlite-ktx:2.3.1")
    implementation("androidx.sqlite:sqlite-framework:2.3.1")

    // Firebase - Import the Bill of Materials (BOM)
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))

    // Add the dependency for Firebase Authentication
    implementation("com.google.firebase:firebase-auth-ktx")

    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")

    implementation("io.coil-kt:coil-compose:2.6.0") // image loading

    // ADD these new OSMDroid lines:
    implementation("org.osmdroid:osmdroid-android:6.1.18")

    // This is still needed for GPS
    implementation("com.google.android.gms:play-services-location:21.2.0")

    // This is still needed for permissions
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    // Used to save a list of GPS points into a string
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("androidx.preference:preference-ktx:1.2.1")

    // --- Gemini AI ---
    implementation("com.google.ai.client.generativeai:generativeai:0.7.0")

    // --- CameraX and ML Kit ---
    val cameraxVersion = "1.5.0"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("androidx.camera:camera-compose:1.3.3")

    // ML Kit
    implementation("com.google.mlkit:image-labeling:17.0.8")

    // Required for WorkManager Alert system
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // --- import for getFlow' ---
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.8.0")

    // Add the dependencies for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries

    // Firebase Cloud Database
    implementation("com.google.firebase:firebase-firestore-ktx")
}