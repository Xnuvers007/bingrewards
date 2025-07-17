plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "xnuvers007.bingrewards"
    compileSdk = 34

    defaultConfig {
        applicationId = "xnuvers007.bingrewards"
        minSdk = 21
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
}