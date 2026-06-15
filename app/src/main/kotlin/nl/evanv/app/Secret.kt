package nl.evanv.app

import java.io.File

fun getMqttPassword(): String =
    loadSecrets()["JACUZZI_MQTT_PASSWORD"]
        ?: throw IllegalStateException("Password not found in secrets.env")

private fun loadSecrets(): Map<String, String> {
    val file = File("../secrets.env")
    if (!file.exists()) {
        throw IllegalStateException("Not found: ${file.absolutePath}")
    }
    val props = mutableMapOf<String, String>()
    file.readLines().forEach { line ->
        if (line.contains("=")) {
            val parts = line.split("=", limit = 2)
            props[parts[0].trim()] = parts[1].trim()
        }
    }
    return props
}
