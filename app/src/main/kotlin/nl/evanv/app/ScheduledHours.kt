package nl.evanv.app

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class ScheduledHours(
    hoursOn: Set<Instant>,
    private val clock: Clock = Clock.systemUTC()
) {
    private val hoursOn: Set<Instant> = hoursOn.map {
        it.floorToHours()
    }.toSet()

    val size: Int get() = hoursOn.size

    fun shouldHeaterBeOn(at: Instant): Boolean =
        hoursOn.contains(at.floorToHours())

    fun shouldHeaterBeOn() = shouldHeaterBeOn(clock.instant())

    fun printScheduledHours(): String {
        return hoursOn.sorted()
            .map {
                it.atZone(ZoneId.of("Europe/Amsterdam"))
            }
            .groupBy(::printDay)
            .mapValues {
                it.value.map(::printHour)
                    .joinToString(", ")
            }
            .mapTo(mutableListOf()) {
                "${it.key} om ${it.value}"
            }
            .joinToString(", ")
    }

    // Format as "woensdag 8 februari"
    val dutchDayFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.of("nl", "NL"))

    private fun printDay(dateTime: ZonedDateTime): String =
        dutchDayFormatter.format(dateTime)

    private fun printHour(dateTime: ZonedDateTime): String = "${dateTime.hour}u"
}

