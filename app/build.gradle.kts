plugins {
    alias(libs.plugins.android.application)

    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.quanlyhocvien"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.quanlyhocvien"
        minSdk = 29
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.security.crypto) // Thư viện mã hóa
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.material) // thư viện meterial component

    // Import the Firebase BoM dùng để quản lý phiên bản Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    // Thêm các phụ thuộc các dịch vụ cụ thể của Firebase
    implementation("com.google.firebase:firebase-auth") // Firebase Authentication
    implementation("com.google.firebase:firebase-storage") // Firebase Storage
    implementation("com.google.firebase:firebase-firestore") // Firebase Firestore

    // Gọi API
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // ọc csv
    implementation("com.opencsv:opencsv:5.5.2")
}