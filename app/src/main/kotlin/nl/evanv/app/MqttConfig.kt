package nl.evanv.app

data class MqttConfig(
    val url: String = getMqttUrl(),
    val user: String = "jacuzzi",
    val password: String = getMqttPassword(),
)

fun getMqttPassword(): String =
    System.getenv("JACUZZI_MQTT_PASSWORD")
        ?: throw IllegalStateException("JACUZZI_MQTT_PASSWORD not set")

fun getMqttUrl(): String =
    System.getenv("JACUZZI_MQTT_URL")
        ?: "tcp://192.168.178.8:1883"
