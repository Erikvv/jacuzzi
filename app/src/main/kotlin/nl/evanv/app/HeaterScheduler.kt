package nl.evanv.app

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class HeaterScheduler(
    private val fetcher: PriceFetcher = PriceFetcher(),
    private val amsterdamZone: ZoneId = ZoneId.of("Europe/Amsterdam"),
    private val nowProvider: () -> ZonedDateTime = { ZonedDateTime.now(amsterdamZone) }
) {
    private var scheduledHours = mutableSetOf<Instant>()
    private var lastFetchDate: LocalDate? = null
    private var fetchedAt14Today = false

    fun shouldHeaterBeOn(): Boolean {
        val now = nowProvider()
        updateScheduleIfNeeded(now)

        val currentHourStart = now.withMinute(0).withSecond(0).withNano(0).toInstant()
        val shouldBeOn = scheduledHours.contains(currentHourStart)

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
}
