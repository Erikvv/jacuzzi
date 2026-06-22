package nl.evanv.app

import java.time.Instant
import java.time.temporal.ChronoUnit

fun Instant.floorToHours(): Instant {
    return truncatedTo(ChronoUnit.HOURS)
}
