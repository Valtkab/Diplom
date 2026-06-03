plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("kapt") // Или id("com.google.devtools.ksp")
    kotlin("plugin.serialization") version "2.0.21" // Версия должна совпадать с твоим Kotlin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.baristamessenger"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.baristamessenger"
        minSdk = 24
        targetSdk = 35
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
        compose = true
    }
}

dependencies {

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
    // --- JETPACK COMPOSE & НАВИГАЦИЯ ---
    // Навигация между экранами (Чаты -> Настройки -> Профиль)
    implementation("androidx.navigation:navigation-compose:2.8.8")
    // --- LOCAL DB: ROOM (Сохраняем чаты на телефоне) ---
    val roomVersion = "2.7.0-alpha01"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    val koinVersion = "4.0.2"
    implementation("io.insert-koin:koin-android:$koinVersion")
    implementation("io.insert-koin:koin-androidx-compose:$koinVersion") // Интеграция с Jetpack Compose
    // --- NETWORK: KTOR (Запросы к серверу и WebSockets для чата в реальном времени) ---
    val ktorVersion = "3.0.3"
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")         // Асинхронный движок для Android
    implementation("io.ktor:ktor-client-websockets:$ktorVersion")  // Двусторонняя связь с сервером без задержек
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion") // Конвертер данных
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion") // Чтение JSON
    // --- ONLINE DB: FIREBASE (Облачная база для переписки в реальном времени) ---
    // Платформа Firebase (BOM) автоматически подберет совместимые версии библиотек
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    // Сама база данных Firestore (куда будут улетать сообщения от разных бариста)
    implementation("com.google.firebase:firebase-firestore-ktx")
    // Библиотека для авторизации пользователей через интернет
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("androidx.navigation:navigation-compose:2.8.0-alpha08") // Версия адаптирована под твой Compose 2.0+
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("com.google.firebase:firebase-storage:21.0.1") // Добавь эту строку
    implementation("com.google.firebase:firebase-firestore:25.0.0")
}