plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "xnuvers007.bingrewards"
    compileSdk = 35

    defaultConfig {
        applicationId = "xnuvers007.bingrewards"
        minSdk = 21
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

//    implementation(libs.appcompat)
//    implementation(libs.material)
//    implementation(libs.activity)
//    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(libs.appcompat.v161)
    implementation(libs.material.v1100)
    implementation(libs.constraintlayout.v214)
    implementation(libs.work.runtime)
    implementation(libs.okhttp)
    implementation(libs.jsoup)
    implementation(libs.recyclerview)
    implementation(libs.lifecycle.runtime.ktx)

        implementation("androidx.appcompat:appcompat:1.6.1")
        implementation("com.google.android.material:material:1.10.0")
        implementation("androidx.constraintlayout:constraintlayout:2.1.4")
        implementation("androidx.work:work-runtime:2.8.1")
        implementation("com.squareup.okhttp3:okhttp:4.12.0")
        implementation("org.jsoup:jsoup:1.16.1")
        implementation("androidx.recyclerview:recyclerview:1.3.2")
        implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
}