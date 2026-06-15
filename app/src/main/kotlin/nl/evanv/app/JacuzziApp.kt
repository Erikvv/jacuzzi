package nl.evanv.app

import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class JacuzziApp {
    private val amsterdamZone = ZoneId.of("Europe/Amsterdam")
    private val fetcher = PriceFetcher()
    private lateinit var controller: JacuzziController

    private var scheduledHours = mutableSetOf<Instant>()
    private var lastFetchDate: LocalDate? = null
    private var fetchedAt14Today = false

    fun run() {
        val secrets = loadSecrets()
        val password = secrets["JACUZZI_MQTT_PASSWORD"] ?: throw IllegalStateException("Password not found in secrets.env")
        controller = JacuzziController("tcp://192.168.178.240:1883", "JacuzziApp", "jacuzzi", password)

        println("[INFO] Starting Jacuzzi Heater Controller Loop...")

        while (true) {
            val now = ZonedDateTime.now(amsterdamZone)
            
            updateScheduleIfNeeded(now)
            checkAndApplyHeaterState(now)

            TimeUnit.MINUTES.sleep(10)
        }
    }

    private fun updateScheduleIfNeeded(now: ZonedDateTime) {
        val today = now.toLocalDate()
        val isAfter14 = now.hour >= 14

        val shouldFetch = lastFetchDate == null ||
                (today != lastFetchDate && isAfter14) ||
                (today == lastFetchDate && isAfter14 && !fetchedAt14Today) ||
                (today != lastFetchDate && lastFetchDate == null)

        if (shouldFetch) {
            println("[DECISION] Conditions met for price fetch (Startup or > 14:00).")

            val start = today.atStartOfDay(amsterdamZone).toInstant()
            val end = today.plusDays(2).atStartOfDay(amsterdamZone).toInstant()

            val allPrices = fetcher.fetchPrices(start, end)

            if (allPrices.isNotEmpty()) {
                val pricesByDay = allPrices.groupBy {
                    it.dateTime.atZone(amsterdamZone).toLocalDate()
                }

                val newScheduledHours = mutableSetOf<Instant>()
                pricesByDay.forEach { (date, prices) ->
                    if (prices.size >= 23) {
                        val cheapest = fetcher.getCheapestHours(prices, 10)
                        println("[INFO] Cheapest 10 hours for $date: ${cheapest.map { it.atZone(amsterdamZone).toLocalTime() }}")
                        newScheduledHours.addAll(cheapest)
                    } else {
                        println("[WARNING] Not enough price data for $date (${prices.size} hours). Skipping schedule for this day.")
                    }
                }

                if (newScheduledHours.isNotEmpty()) {
                    scheduledHours = newScheduledHours
                    lastFetchDate = today
                    fetchedAt14Today = isAfter14
                    println("[INFO] Schedule updated. Total scheduled hours in memory: ${scheduledHours.size}")
                }
            } else {
                println("[WARNING] No prices fetched. Will retry in 10 minutes.")
            }
        }

        // Reset 14:00 flag if day changed
        if (today != lastFetchDate && lastFetchDate != null) {
            fetchedAt14Today = false
        }
    }

    private fun checkAndApplyHeaterState(now: ZonedDateTime) {
        val currentHourStart = now.withMinute(0).withSecond(0).withNano(0).toInstant()
        val shouldBeOn = scheduledHours.contains(currentHourStart)

        println("[INFO] Current time: ${now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}. Should heater be ON? $shouldBeOn")

        try {
            controller.ensureState(if (shouldBeOn) "ON" else "OFF")
        } catch (e: Exception) {
            println("[ERROR] Error updating heater state: ${e.message}")
        }
    }

    private fun loadSecrets(): Map<String, String> {
        val file = File("secrets.env")
        if (!file.exists()) return emptyMap()
        val props = mutableMapOf<String, String>()
        file.readLines().forEach { line ->
            if (line.contains("=")) {
                val parts = line.split("=", limit = 2)
                props[parts[0].trim()] = parts[1].trim()
            }
        }
        return props
    }
}
