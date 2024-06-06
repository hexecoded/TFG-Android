plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.skincancer.skincancerapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.skincancer.skincancerapp"
        minSdk = 30
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

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // https://mvnrepository.com/artifact/org.pytorch/pytorch_android_torchvision_lite
    implementation("org.pytorch:pytorch_android_torchvision:2.1.0")
    implementation("org.pytorch:pytorch_android:2.1.0")
    implementation("com.vanniktech:android-image-cropper:4.5.0")

}