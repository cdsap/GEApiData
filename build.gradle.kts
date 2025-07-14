import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.2.0"
    application
    `maven-publish`
    `signing`
    id("org.jlleitschuh.gradle.ktlint") version "13.0.0"
    id("com.vanniktech.maven.publish") version "0.34.0"
}

group = "io.github.cdsap"
version = "0.3.3"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-client-core:2.3.13")
    implementation("io.ktor:ktor-client-cio:2.3.13")
    implementation("io.ktor:ktor-client-auth:2.3.13")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.13")
    implementation("io.ktor:ktor-serialization-gson:2.3.13")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.compileJava {
    options.release.set(17)
}

configure<JavaPluginExtension> {
    withJavadocJar()
    withSourcesJar()
}

application {
    mainClass.set("MainKt")
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates("io.github.cdsap", "geapi-data", "0.3.3")

    pom {
        scm {
            connection.set("scm:git:git://github.com/cdsap/GEApiData/")
            url.set("https://github.com/cdsap/GEApiData/")
        }
        name.set("data")
        url.set("https://github.com/cdsap/GEApiData/")
        description.set(
            "DV Api Data layer providing BuildScans based on a filter",
        )
        licenses {
            license {
                name.set("The MIT License (MIT)")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("cdsap")
                name.set("Inaki Villar")
            }
        }
    }
}

if (extra.has("signing.keyId")) {
    afterEvaluate {
        configure<SigningExtension> {
            (
                extensions.getByName("publishing") as
                    PublishingExtension
            ).publications.forEach {
                sign(it)
            }
        }
    }
}
