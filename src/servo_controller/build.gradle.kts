import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.21"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:latest.release")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:latest.release")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:latest.release")
    implementation("com.fazecast:jSerialComm:[2.0.0,3.0.0)")

}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}