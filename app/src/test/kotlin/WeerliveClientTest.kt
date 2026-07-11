package nl.evanv.app

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeerliveClientTest {

    @Test
    fun testSaneTemperature() {
        val client = WeerliveClient()

        val temp = client.fetchAverageTemperatureTomorrow()
        
        // Sanity check
        assertTrue(temp > -30.0 && temp < 50.0, "Temperature $temp should be within sane bounds")
    }
}
