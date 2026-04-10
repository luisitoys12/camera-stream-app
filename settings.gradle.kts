pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        // flatDir is declared here (not in app/build.gradle.kts) to comply with
        // FAIL_ON_PROJECT_REPOS: local AARs such as NodeMediaClient.aar downloaded
        // by CI into app/libs are resolved from this settings-level repository.
        flatDir { dirs("app/libs") }
    }
}
rootProject.name = "CameraStream"
include(":app")
