
plugins {
    // Apply the java-library plugin for API and implementation separation.
    `java-library`
    //paper
    id("io.papermc.paperweight.userdev") version "1.5.5"
}

group = "de.greensurvivors"
version = "0.0.1-SNAPSHOT"
description = "A little plugin to test GUIs"
// this is the minecraft major version. If you need a subversion like 1.20.1,
// change it in the dependencies section as this is also used as the api version of the plugin.yml
val mainMCVersion by extra("1.20")

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 17 on systems that only have JDK 8 installed for example.
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
    mavenLocal()

    //paper
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    paperweight.paperDevBundle("1.20.1-R0.1-SNAPSHOT")

    //please note: right now the lib is not avileble via maven central, one it is use the libraries feature in the plugin.yml and compile only
    //takes from project
    api(project(":GreenUIFramework"))
    //takes from maven
    //compileOnly("de.greensurvivors:GreenUIFramework:0.0.2-SNAPSHOT")
}

tasks {
    // Configure reobfJar to run when invoking the build task
    assemble {
        dependsOn(reobfJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything

        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release.set(17)
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything

        expand(
            "version" to project.version,
            "description" to project.description,
            "apiVersion" to mainMCVersion
        )
    }

    /*
    reobfJar {
      // This is an example of how you might change the output location for reobfJar. It's recommended not to do this
      // for a variety of reasons, however it's asked frequently enough that an example of how to do it is included here.
      outputJar.set(layout.buildDirectory.file("libs/PaperweightTestPlugin-${project.version}.jar"))
    }
     */
}