package nl.evanv.app

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScheduledHoursTest {

    @Test
    fun `test shouldHeaterBeOn with specific instant`() {
        val hour1 = Instant.parse("2023-10-27T10:00:00Z")
        val scheduledHours = ScheduledHours(setOf(hour1))

        assertTrue(scheduledHours.shouldHeaterBeOn(hour1), "Should be on at the exact scheduled hour")
        assertTrue(scheduledHours.shouldHeaterBeOn(hour1.plusSeconds(1800)), "Should be on 30 minutes into the scheduled hour")
        assertFalse(scheduledHours.shouldHeaterBeOn(hour1.minusSeconds(1)), "Should be off 1 second before the scheduled hour")
        assertFalse(scheduledHours.shouldHeaterBeOn(hour1.plus(1, ChronoUnit.HOURS)), "Should be off in the next unscheduled hour")
    }

    @Test
    fun `test shouldHeaterBeOn with clock`() {
        val hour1 = Instant.parse("2023-10-27T10:00:00Z")
        val clock = Clock.fixed(hour1.plusSeconds(1800), ZoneId.of("UTC"))
        val scheduledHours = ScheduledHours(setOf(hour1), clock)

        assertTrue(scheduledHours.shouldHeaterBeOn(), "Should be on based on the provided clock")
    }

    @Test
    fun `test printScheduledHours handles multiple hours and days`() {
        val amsterdamZone = ZoneId.of("Europe/Amsterdam")
        // Use fixed dates in Amsterdam zone
        // Friday Oct 27, 2023
        val t1 = ZonedDateTime.of(2023, 10, 27, 10, 0, 0, 0, amsterdamZone).toInstant()
        val t2 = ZonedDateTime.of(2023, 10, 27, 11, 0, 0, 0, amsterdamZone).toInstant()
        // Saturday Oct 28, 2023
        val t3 = ZonedDateTime.of(2023, 10, 28, 9, 0, 0, 0, amsterdamZone).toInstant()

        val scheduledHours = ScheduledHours(setOf(t2, t1, t3)) // Unordered input
        
        val result = scheduledHours.printScheduledHours()
        
        val expected = "vrijdag 27 oktober om 10u, 11u, zaterdag 28 oktober om 9u"
        assertEquals(expected, result)
    }

    @Test
    fun `test printScheduledHours with empty set`() {
        val scheduledHours = ScheduledHours(emptySet())
        assertEquals("", scheduledHours.printScheduledHours())
    }

    @Test
    fun `test constructor floors input hours`() {
        val hour1 = Instant.parse("2023-10-27T10:30:00Z")
        val scheduledHours = ScheduledHours(setOf(hour1))
        
        assertTrue(scheduledHours.shouldHeaterBeOn(Instant.parse("2023-10-27T10:00:00Z")), "Should be on at the beginning of the floored hour")
        assertTrue(scheduledHours.shouldHeaterBeOn(Instant.parse("2023-10-27T10:59:59Z")), "Should be on at the end of the floored hour")
    }
}
