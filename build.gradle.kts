import com.android.build.gradle.internal.dsl.decorator.SupportedPropertyType.Var.String.type

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.1")
    }
}
plugins {
    id("com.android.application") version ("8.3.2") apply false
    id("com.android.library") version ("8.3.2") apply false
    alias(libs.plugins.googleAndroidLibrariesMapsplatformSecretsGradlePlugin) apply false
}
