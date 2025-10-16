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
        // MPAndroidChart library එක සොයාගැනීමට මෙම පේළිය අත්‍යවශ්‍යයි
        maven { url = uri("https://jitpack.io") }
    }
}
rootProject.name = "labexam3"
include(":app")