plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    compileSdk = 34
    namespace = "cn.fkj233.ui"
    defaultConfig {
        minSdk = 31
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.majorVersion
    }
}

dependencies {
    implementation("androidx.annotation:annotation:1.8.2")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.arch.core:core-runtime:2.2.0")
}
