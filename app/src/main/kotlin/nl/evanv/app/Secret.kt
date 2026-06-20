package nl.evanv.app

import java.io.File

fun getMqttPassword(): String =
    System.getenv("JACUZZI_MQTT_PASSWORD")
        ?: throw IllegalStateException("JACUZZI_MQTT_PASSWORD not se")
