buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10")
        classpath("com.android.tools.build:gradle:7.0.4")
        classpath("io.realm:realm-gradle-plugin:10.11.1")
//        classpath("com.google.gms:google-services:4.3.13")
//        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.2")
    }
}
plugins {
    id("com.android.application") version "7.4.2" apply false
    id("com.android.library") version "7.4.2" apply false
    id("org.jetbrains.kotlin.android") version "1.8.0" apply false
}

task("clean", Delete::class) {
    delete(rootProject.buildDir)
}