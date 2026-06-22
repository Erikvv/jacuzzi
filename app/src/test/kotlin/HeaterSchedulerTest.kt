package nl.evanv.app

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HeaterSchedulerTest {
    @Test
    fun myFun() {
        val scheduler = HeaterScheduler()
        scheduler.shouldHeaterBeOn()
    }

    @Test
    fun `test shouldHeaterBeOn returns true when hour is scheduled`() {
        val amsterdamZone = ZoneId.of("Europe/Amsterdam")
        val fixedNow = ZonedDateTime.of(2023, 10, 27, 10, 0, 0, 0, amsterdamZone)
        
        val fakeFetcher = object : PriceFetcher() {
            override fun fetchPrices(start: Instant, end: Instant): List<PricePoint> {
                return (0..23).map { i ->
                    PricePoint(fixedNow.withHour(i).toInstant(), i.toDouble())
                }
            }
            override fun getCheapestHours(prices: List<PricePoint>, count: Int): Set<Instant> {
                // Return the fixedNow as one of the cheapest
                return setOf(fixedNow.toInstant())
            }
        }
        
        val scheduler = HeaterScheduler(
            fetcher = fakeFetcher,
            amsterdamZone = amsterdamZone,
            nowProvider = { fixedNow }
        )
        
        assertTrue(scheduler.shouldHeaterBeOn())
    }

    @Test
    fun `test shouldHeaterBeOn returns false when hour is not scheduled`() {
        val amsterdamZone = ZoneId.of("Europe/Amsterdam")
        val fixedNow = ZonedDateTime.of(2023, 10, 27, 10, 0, 0, 0, amsterdamZone)
        val someOtherTime = fixedNow.plusHours(1)
        
        val fakeFetcher = object : PriceFetcher() {
            override fun fetchPrices(start: Instant, end: Instant): List<PricePoint> {
                return (0..23).map { i ->
                    PricePoint(fixedNow.withHour(i).toInstant(), i.toDouble())
                }
            }
            override fun getCheapestHours(prices: List<PricePoint>, count: Int): Set<Instant> {
                return setOf(someOtherTime.toInstant())
            }
        }
        
        val scheduler = HeaterScheduler(
            fetcher = fakeFetcher,
            amsterdamZone = amsterdamZone,
            nowProvider = { fixedNow }
        )
        
        assertFalse(scheduler.shouldHeaterBeOn())
    }
}
