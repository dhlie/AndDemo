// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.ksp) apply false
    // Add the dependency for the Google services Gradle plugin
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics.gradle) apply false
}
