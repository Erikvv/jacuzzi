package nl.evanv.app

import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONObject

/**
 * Controls the heater switch over MQTT.
 * State of the switch in tracked in Zigbee2MQTT.
 * You can send the same command multiple times,
 * it will only work the first time.
 */
class HeaterSwitch(
    private val client: MqttClient,
    private val topic: String = "zigbee2mqtt/jacuzzi_fingerbot/set",
) {
    companion object {
        fun create(
            config: MqttConfig = MqttConfig(),
        ): HeaterSwitch {
            try {
                val client = MqttClient(config.url, config.user, MemoryPersistence())
                val options = MqttConnectOptions().apply {
                    userName = config.user
                    password = config.password.toCharArray()
                    isCleanSession = true
                    connectionTimeout = 10
                    isAutomaticReconnect = true
                }
                println("Connecting to MQTT broker at ${config.url}...")
                client.connect(options)
                println("Connected to MQTT.")
                return HeaterSwitch(client)
            } catch (e: Exception) {
                println("[ERROR] Failed to connect to MQTT: ${e.message}")
                throw e
            }
        }
    }

    fun switchOn() {
        switch("ON")
    }

    fun switchOff() {
        switch("OFF")
    }

    private fun switch(state: String) {
        val payload = JSONObject().apply {
            put("state", state)
        }
        val message = MqttMessage(payload.toString().toByteArray()).apply {
            qos = 1
        }
        println("Sending $message to $topic")
        client.publish(topic, message)
    }
}
