plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-android")
    id("kotlin-kapt")
    id("realm-android")
}

android {
    compileSdk = BuildConfig.compileSdk

    defaultConfig {
        minSdk = BuildConfig.minSdk
        targetSdk = BuildConfig.targetSdk

        testInstrumentationRunner = BuildConfig.testInstrumentationRunner

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }

        kapt {
            arguments {
                arg("AROUTER_MODULE_NAME", project.name)
            }
        }
    }

    buildFeatures {
        viewBinding = true
    }

    sourceSets {
        getByName("main") {
            res.srcDirs("src/main/res")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = Jdk.sourceCompatibility
        targetCompatibility = Jdk.targetCompatibility
    }
    kotlinOptions {
        jvmTarget = Jdk.jvmTarget
    }
}

dependencies {
    implementation(AndroidX.androidXCoreKtx)
    implementation(AndroidX.androidXAppcompat)
    implementation(Google.material)
    testImplementation(Testing.junit)
    androidTestImplementation(Testing.androidXJunit)
    androidTestImplementation(Testing.espresso)

    api(project(":base"))

    //ARouter
    api(ThirdPart.arouter)
    kapt(ThirdPart.arouter_compiler)
    api("com.lzy.net:okgo:3.0.4")
}