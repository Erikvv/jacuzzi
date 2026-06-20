plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")

    // Apply the Application plugin to add support for building an executable JVM application.
    application
}

dependencies {
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    implementation("org.json:json:20240303")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")

    testImplementation(kotlin("test"))
    testImplementation(libs.org.junit.jupiter.junit.jupiter.api)
    testImplementation(libs.org.junit.jupiter.junit.jupiter.params)
    testRuntimeOnly(libs.org.junit.jupiter.junit.jupiter.engine)
    testRuntimeOnly(libs.org.junit.platform.junit.platform.launcher)
}

application {
    // Define the Fully Qualified Name for the application main class
    // (Note that Kotlin compiles `App.kt` to a class with FQN `com.example.app.AppKt`.)
    mainClass = "nl.evanv.app.MainKt"
}

fun loadEnvFile(fileName: String = "secrets.env"): Map<String, String> {
    val envFile = rootProject.file(fileName)

    if (!envFile.exists()) {
        throw Exception("secrets.env file missing")
    }

    return envFile.readLines()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .mapNotNull { line ->
            val separatorIndex = line.indexOf("=")
            if (separatorIndex == -1) {
                null
            } else {
                val key = line.substring(0, separatorIndex).trim()
                val value = line.substring(separatorIndex + 1).trim()
                    .removeSurrounding("\"")
                    .removeSurrounding("'")

                key to value
            }
        }
        .toMap()
}

val secretEnv = loadEnvFile()

tasks.withType<Test>().configureEach {
    environment(secretEnv)
}

tasks.withType<JavaExec>().configureEach {
    environment(secretEnv)
}
