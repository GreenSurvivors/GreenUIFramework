pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

rootProject.name = "GreenUIFrameworkProject"

include("lib","gui-example")
project(":lib").name = "GreenUIFramework"