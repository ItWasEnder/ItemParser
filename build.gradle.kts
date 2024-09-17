import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.util.Properties

plugins {
    kotlin("jvm") version "2.0.20-RC"
    id("io.github.goooler.shadow") version "8.1.7"
    id("maven-publish")
}

val localProperties = file("local.properties")
if (localProperties.exists()) {
    val properties = Properties().apply { load(localProperties.inputStream()) }
    extra.set("repoUser", properties.getProperty("repoUser"))
    extra.set("repoPassword", properties.getProperty("repoPassword"))
}

group = "tv.ender.itemparser"
version = "1.0.5"

val GSON_VERSION = "2.8.9"

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
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // add gson
    compileOnly("com.google.code.gson:gson:$GSON_VERSION")
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.withType<ShadowJar> {
    // Ensure you name the jar appropriately
    archiveClassifier.set("all")
    minimize()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks.shadowJar.get()) {
                classifier = null // Ensure the classifier is set to null
            }

            groupId = project.group.toString()
            artifactId = rootProject.name
            version = project.version.toString()
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
    }
}