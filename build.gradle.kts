import java.util.Properties
import java.util.Locale

plugins {
    kotlin("jvm") version "2.0.20-RC"
    id("io.github.goooler.shadow") version "8.1.7"
    id("xyz.jpenilla.run-paper") version "2.3.0"
    id("maven-publish")
}

val localProperties = file("local.properties")
if (localProperties.exists()) {
    val properties = Properties().apply { load(localProperties.inputStream()) }
    extra.set("repoUser", properties.getProperty("repoUser"))
    extra.set("repoPassword", properties.getProperty("repoPassword"))
}

group = "tv.ender.itemparser"
// Default version for local/dev builds. CI publishing should override via `-PreleaseVersion=...`
version = (findProperty("releaseVersion") as String?) ?: "1.3.1-SNAPSHOT"

val gsonVersion = "2.8.9"
val mockkVersion = "1.13.12"
val paperVersion = "1.21.1-R0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:${mockkVersion}")
    testImplementation("io.papermc.paper:paper-api:$paperVersion")

    compileOnly("io.papermc.paper:paper-api:$paperVersion")
    compileOnly(kotlin("stdlib-jdk8"))

    // add gson
    compileOnly("com.google.code.gson:gson:$gsonVersion")
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks {
    test {
        useJUnitPlatform()
    }

    runServer {
        minecraftVersion("1.21.1")
    }

    build {
        dependsOn("shadowJar")
    }

    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    shadowJar {
        isEnableRelocation = true
        relocationPrefix = "${project.group}.libraries"
        // Publish/use the shaded jar as the primary artifact (no classifier).
        archiveClassifier.set("")
        minimize()
    }

    // Keep the non-shaded jar available as a secondary artifact for debugging/reference.
    jar {
        archiveClassifier.set("original")
    }

    compileJava {
        options.encoding = "UTF-8"
    }
}

publishing {
    val githubRepo = (System.getenv("GITHUB_REPOSITORY") ?: "ender/ItemParser").trim()
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks.shadowJar.get()) {
                classifier = null // publish shaded jar with NO classifier
            }

            groupId = project.group.toString()
            // GitHub Packages (Maven) rejects uppercase artifact IDs and returns HTTP 422.
            artifactId = rootProject.name.lowercase(Locale.ROOT)
            version = project.version.toString()

            pom {
                name.set(rootProject.name)
                description.set("Minecraft/Paper item parsing utilities")
                url.set("https://github.com/$githubRepo")
            }
        }
    }
    repositories {
        maven {
            name = "ender-private"
            url = uri("https://repo.ender.tv/public")
            credentials {
                username = findProperty("repoUser") as String? ?: System.getenv("REPO_USER")
                password = findProperty("repoPassword") as String? ?: System.getenv("REPO_PASSWORD")
            }
        }

        // GitHub Packages (Maven). Used by the GitHub Actions workflow.
        // Docs: https://docs.github.com/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/$githubRepo")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}