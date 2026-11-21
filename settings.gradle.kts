pluginManagement {
    val localGradlePlugin = file("../manticordrebuild/plugin-gradle")
    if (localGradlePlugin.isDirectory) includeBuild(localGradlePlugin)

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://raw.git.hoodnet.work/microcord/microcord/maven")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://raw.git.hoodnet.work/microcord/microcord/maven")
    }
}

rootProject.name = "PermissionsViewer"
