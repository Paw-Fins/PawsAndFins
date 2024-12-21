pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "7.4.2"
        id("org.jetbrains.kotlin.android") version "2.0.0"
        id("com.google.gms.google-services") version "4.4.2"  // Ensure correct plugin version
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Myapp"
include(":app")
