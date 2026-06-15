package nl.evanv.app

data class MqttConfig(
    val url: String = "tcp://192.168.178.240:1883",
    val user: String = "jacuzzi",
    val password: String = getMqttPassword(),
)
