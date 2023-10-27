plugins {
    // Apply the java-library plugin for API and implementation separation.
    `java-library`
    //paper
    id("io.papermc.paperweight.userdev") version "1.5.5"
    //publishing
    `maven-publish`
}

group = "de.greensurvivors"
version = "0.0.2-SNAPSHOT"
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

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("GreenUIFramework") {
            from(components["java"])
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
            name = "greensurvivors"
            url = uri("https://maven.greensurvivors.de/releases")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}
