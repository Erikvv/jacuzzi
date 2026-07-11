package nl.evanv.app

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class HeaterScheduler(
    private val fetcher: PriceFetcher = PriceFetcher(),
    private val weerliveClient: WeerliveClient = WeerliveClient(),
    private val amsterdamZone: ZoneId = ZoneId.of("Europe/Amsterdam"),
    private val nowProvider: () -> ZonedDateTime = { ZonedDateTime.now(amsterdamZone) }
) {
    private var scheduledHours = ScheduledHours(emptySet())
    private var lastFetchDate: LocalDate? = null
    private var fetchedAt14Today = false

    fun shouldHeaterBeOn(): Boolean {
        val now = nowProvider()
        updateScheduleIfNeeded(now)

        val shouldBeOn = scheduledHours.shouldHeaterBeOn(now.toInstant())

        println("[INFO] Current time: ${now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}. Should heater be ON? $shouldBeOn")

        return shouldBeOn
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

            val start = now.toInstant().floorToHours()
            val end = today.plusDays(2).atStartOfDay(amsterdamZone).toInstant()

            val allPrices = fetcher.fetchPrices(start, end)

            if (allPrices.isNotEmpty()) {
                val hoursPerDayToRun = getNumHoursPerDayToRun()
                val numberOfHoursToRun = ((hoursPerDayToRun / 24.0) * allPrices.size).toInt()

                val cheapestHours = fetcher.getCheapestHours(allPrices, numberOfHoursToRun)

                scheduledHours = ScheduledHours(cheapestHours)
                lastFetchDate = today
                fetchedAt14Today = isAfter14
                println("[INFO] Schedule updated: ${this.scheduledHours.printScheduledHours()}")
            } else {
                println("[WARNING] No prices fetched. Will retry in 10 minutes.")
            }
        }

        // Reset 14:00 flag if day changed
        if (today != lastFetchDate && lastFetchDate != null) {
            fetchedAt14Today = false
        }
    }

    private fun getNumHoursPerDayToRun(): Double {
        val temperature = try {
            weerliveClient.fetchAverageTemperatureTomorrow()
        } catch (e: Exception) {
            println(e);
            -10.0
        }

        // at 0 degrees it will run for 12 hours
        // at 20 degrees it will run for 6 hours
        return 12.0 - temperature * (6.0 / 20.0)
    }
}
