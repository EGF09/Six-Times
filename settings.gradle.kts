// Eklenti yönetimi yapılandırması: Gradle eklentilerinin nereden indirileceğini belirler.
pluginManagement {
    repositories {
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

plugins {
    // Gradle araç zinciri çözücü eklentisi.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// Bağımlılık çözümleme yönetimi: Projedeki tüm modüller için ortak depo (repository) yapılandırması.
dependencyResolutionManagement {
    // Proje bazlı depolar yerine merkezi yapılandırmayı zorunlu kılar.
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// Proje adı ve dahil edilen modüller.
rootProject.name = "6Times"
include(":app")