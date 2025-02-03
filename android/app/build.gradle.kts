plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.devtools.ksp)
}

android {
    namespace = "com.example.ftpsclient"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.ftpsclient"
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
        viewBinding = true
    }
    room {
        schemaDirectory("$projectDir/schema")
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
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.commons.net)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

//
//    implementation 'androidx.core:core-ktx:1.9.0'
//    implementation 'androidx.appcompat:appcompat:1.6.1'
//    implementation 'com.google.android.material:material:1.9.0'
//    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
//    implementation 'commons-net:commons-net:3.9.0'
//    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1'
//    implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.6.1'
//    implementation 'androidx.room:room-runtime:2.5.1'
//    implementation 'androidx.room:room-ktx:2.5.1'
//    kapt 'androidx.room:room-compiler:2.5.1'
//    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4'

}

configurations {
    all {
            exclude(group = "com.intellij", module = "annotations")

    }
}