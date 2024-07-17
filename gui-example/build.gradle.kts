plugins {
    // Apply the java-library plugin for API and implementation separation.
    `java-library`
    //paper
    id("io.papermc.paperweight.userdev") version "1.7.1"
}

group = "de.greensurvivors"
version = "0.0.2-SNAPSHOT"
description = "A little plugin to test GUIs"
// this is the minecraft major version. If you need a subversion like 1.20.1,
// change it in the dependencies section as this is also used as the api version of the plugin.yml
val mcVersion by extra("1.21")

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 17 on systems that only have JDK 8 installed for example.
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
    mavenLocal()

    //paper
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    // GreenUI framework
    maven {
        url = uri("https://maven.greensurvivors.de")
    }
}

dependencies {
    paperweight.paperDevBundle("$mcVersion-R0.1-SNAPSHOT")

    //please note: right now the lib is not avileble via maven central, one it is use the libraries feature in the plugin.yml and compile only
    //takes from project
    api(project(":GreenUIFramework"))
    //takes from maven
    //api("de.greensurvivors","GreenUIFramework","0.0.4", classifier = "dev")
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything

        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release.set(21)
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything

        //Todo fix template like plugin template
        expand(
            "version" to project.version,
            "description" to project.description,
            "apiVersion" to mcVersion
        )
    }
}