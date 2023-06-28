plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-android")
    id("kotlin-kapt")
    id("maven-publish")
    id("realm-android")
//    id("com.google.gms.google-services")
//    id("com.google.firebase.crashlytics")
}

android {
    compileSdk = BuildConfig.compileSdk

    defaultConfig {
        applicationId = BuildConfig.applicationId
        minSdk = BuildConfig.minSdk
        targetSdk = BuildConfig.targetSdk
        versionCode = BuildConfig.versionCode
        versionName = BuildConfig.versionName

        testInstrumentationRunner = BuildConfig.testInstrumentationRunner

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a","x86")
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
            jniLibs.srcDirs("libs")
            assets.srcDirs("src/main/assets", "src/main/assets/", "src\\main\\assets", "src\\main\\assets")
            res.srcDirs("src/main/res")
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("tchatgpt.jks")
            storePassword = "tchatgpt20230321"
            keyAlias = "key0"
            keyPassword = "tchatgpt20230321"
        }
        getByName("debug") {
            storeFile = file("tchatgpt.jks")
            storePassword = "tchatgpt20230321"
            keyAlias = "key0"
            keyPassword = "tchatgpt20230321"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = false
            isDebuggable = true
            isShrinkResources = false
            manifestPlaceholders["CHANNEL"] = "def"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("boolean", "is_debug", BuildConfig.debug_is_debug)
            var debug_is_dev = BuildConfig.debug_is_dev.toBoolean()
            buildConfigField("boolean", "is_dev", debug_is_dev.toString())
            buildConfigField("boolean", "is_proxy", BuildConfig.debug_is_proxy)
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isDebuggable = false
            isShrinkResources = true
            manifestPlaceholders["CHANNEL"] = "def"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("boolean", "is_debug", BuildConfig.release_is_debug)
            buildConfigField("boolean", "is_dev", BuildConfig.release_is_dev)
            buildConfigField("boolean", "is_proxy", BuildConfig.release_is_proxy)
        }
    }

    android.applicationVariants.all {
        outputs.all {
            if (this is com.android.build.gradle.internal.api.ApkVariantOutputImpl) {
                this.outputFileName =
                    "tchatgpt_${versionName}_${Time.getDate()}_${buildType.name}_${BuildConfig.debug_is_dev}.apk"
            }
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

    implementation(project(":common"))

    //ARouter
    implementation("com.alibaba:arouter-api:1.5.2")
    kapt("com.alibaba:arouter-compiler:1.5.1")

    //firebase
//    api(platform(ThirdPart.firebase_bom))
//    api(ThirdPart.firebase_analytics)
//    api(ThirdPart.firebase_crashlytics)
}