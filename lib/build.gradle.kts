plugins {
    // Apply the java-library plugin for API and implementation separation.
    `java-library`
    //paper
    id("io.papermc.paperweight.userdev") version "1.5.5"
    //publishing
    `maven-publish`
}

group = "de.greensurvivors"
version = "0.0.3-SNAPSHOT"
description = "A framework to create GUIs "

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 17 on systems that only have JDK 8 installed for example.
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()

    //paper
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")

    // This dependency is exported to consumers, that is to say found on their compile classpath.
    //api("org.apache.commons:commons-math3:3.6.1")

    // This dependency is used internally, and not exposed to consumers on their own compile classpath.
    //implementation("com.google.guava:guava:31.0.1-jre")

    paperweight.paperDevBundle("1.20.1-R0.1-SNAPSHOT")
}

tasks {
    // Configure reobfJar to run when invoking the build task
    assemble {
        dependsOn(reobfJar)
    }

    publish {
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
    }

    /*
    reobfJar {
      // This is an example of how you might change the output location for reobfJar. It's recommended not to do this
      // for a variety of reasons, however it's asked frequently enough that an example of how to do it is included here.
      outputJar.set(layout.buildDirectory.file("libs/GreenUIFramework-${project.version}.jar"))
    }
     */

    test {
        // Use JUnit Platform for unit tests.
        useJUnitPlatform()
    }
}

publishing {
    publications {
        create<MavenPublication>("GreenUIFramework") {
            from(components["java"]) // dev
            artifact(tasks.jar.get().outputs.files.singleFile) // production
            pom {
                name.set("GreenUIFramework")
                description.set("A handy lib to create inventory based Menus")
                url.set("https://github.com/GreenSurvivors/GreenUIFramework")
                licenses {
                    license {
                        name.set("GNU General Public License v3.0")
                        url.set("https://www.gnu.org/licenses/gpl-3.0.html")
                    }
                }
                developers {
                    developer {
                        name.set("GreenSurvivors Team")
                    }
                }
            }
        }
    }

    repositories {
        maven {
            name = "greensurvivorsMaven"
            url = uri("https://maven.greensurvivors.de/releases")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}
