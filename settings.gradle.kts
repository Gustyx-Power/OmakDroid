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
    }
}

rootProject.name = "OmakDroid"
include(":app")
include(":core:system")
include(":core:ui")
include(":core:common")
include(":feature:installer")
include(":feature:desktop")
