package nl.evanv.app

import java.util.concurrent.TimeUnit

class JacuzziApp {
    private val heaterSwitch = HeaterSwitch.create()
    private val scheduler = HeaterScheduler()

    fun run() {
        println("[INFO] Starting Jacuzzi Heater Controller Loop...")

        while (true) {
            val shouldBeOn = scheduler.shouldHeaterBeOn()

            try {
                if (shouldBeOn) {
                    heaterSwitch.switchOn()
                } else {
                    heaterSwitch.switchOff()
                }
            } catch (e: Exception) {
                println("[ERROR] Error updating heater state: ${e.message}")
            }

            TimeUnit.MINUTES.sleep(10)
        }
    }
}
