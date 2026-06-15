package nl.evanv.app

import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONObject

class JacuzziController(
    private val broker: String,
    private val clientId: String,
    private val user: String,
    private val pass: String
) {
    private var client: MqttClient? = null
    private val topicSet = "zigbee2mqtt/jacuzzi_fingerbot"
    private var lastState: String? = null

    private fun connect() {
        try {
            val newClient = MqttClient(broker, clientId, MemoryPersistence())
            val options = MqttConnectOptions().apply {
                userName = user
                password = pass.toCharArray()
                isCleanSession = true
                connectionTimeout = 10
                isAutomaticReconnect = true
            }
            println("Connecting to MQTT broker at $broker...")
            newClient.connect(options)
            println("Connected to MQTT.")
            client = newClient
        } catch (e: Exception) {
            println("[ERROR] Failed to connect to MQTT: ${e.message}")
            throw e
        }
    }

    fun ensureState(desiredState: String) {
        if (lastState != desiredState) {
            println("[DECISION] State change needed. Current (memory): $lastState, Desired: $desiredState. Sending command.")
            try {
                if (client == null || !client!!.isConnected) {
                    connect()
                }
                sendState(desiredState)
                lastState = desiredState
                println("[INFO] Heater state updated to $desiredState.")
            } catch (e: Exception) {
                println("[ERROR] Could not update heater state: ${e.message}")
            }
        }
    }

    private fun sendState(state: String) {
        val payload = JSONObject().apply {
            put("state", "toggle")
        }
        val message = MqttMessage(payload.toString().toByteArray()).apply {
            qos = 1
        }
        client?.publish(topicSet, message)
    }

    fun disconnect() {
        try {
            client?.let {
                if (it.isConnected) it.disconnect()
            }
        } catch (e: Exception) {
            // Ignore
        }
    }
}
