plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.aprendeaprender"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.aprendeaprender"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        fun escapedProperty(name: String): String {
            return (project.findProperty(name) as? String)
                ?.replace("\\", "\\\\")
                ?.replace("\"", "\\\"")
                ?: ""
        }

        val Apis = escapedProperty("APIS")
        val apiBaseUrl = escapedProperty("API_BASE_URL").ifBlank { "http://10.0.2.2:8080/" }
        val gemmaModelSizeBytes = (project.findProperty("GEMMA_MODEL_SIZE_BYTES") as? String)
            ?.toLongOrNull()
            ?: 0L

        buildConfigField("String", "APIS", "\"$Apis\"")
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
        buildConfigField("String", "OPENROUTER_MODEL", "\"openrouter/free\"")

        buildConfigField("String", "GEMMA_MODEL_URL", "\"${escapedProperty("GEMMA_MODEL_URL")}\"")
        buildConfigField("String", "GEMMA_MODEL_TOKEN", "\"${escapedProperty("GEMMA_MODEL_TOKEN")}\"")
        buildConfigField("String", "GEMMA_MODEL_SHA256", "\"${escapedProperty("GEMMA_MODEL_SHA256")}\"")
        buildConfigField("long", "GEMMA_MODEL_SIZE_BYTES", "${gemmaModelSizeBytes}L")

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
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.work:work-runtime-ktx:2.11.2")
    implementation("androidx.compose.material:material-icons-extended")
    implementation(platform("androidx.compose:compose-bom:2024.02.01"))
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("org.jsoup:jsoup:1.17.2")
    implementation(platform("com.google.firebase:firebase-bom:34.12.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.ai.edge.litertlm:litertlm-android:0.11.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
