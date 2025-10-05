pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // --- BARIS INI YANG DITAMBAHKAN ---
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "InfoIn"
include(":app")