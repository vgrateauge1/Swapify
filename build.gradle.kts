// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.6.0" apply false
    id ("androidx.navigation.safeargs.kotlin") version "2.7.5" apply false
    id("com.google.devtools.ksp") version "1.9.10-1.0.13" apply false
    id("com.google.dagger.hilt.android") version "2.49" apply false
}

buildscript {
    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.5")
        classpath ("com.google.dagger:hilt-android-gradle-plugin:2.49")

    }
}
