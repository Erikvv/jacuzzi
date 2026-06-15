package nl.evanv.app

import org.json.JSONObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

data class PricePoint(val dateTime: Instant, val tariff: Double)

class PriceFetcher {
    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    fun fetchPrices(start: Instant, end: Instant): List<PricePoint> {
        val url = "https://api.anwb.nl/energy/energy-services/v2/tarieven/electricity?startDate=$start&endDate=$end&interval=HOUR"
        println("[INFO] Fetching prices from $url")
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Accept", "application/json")
            .GET()
            .build()

        return try {
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() != 200) {
                println("[ERROR] Failed to fetch prices: ${response.statusCode()} ${response.body()}")
                return emptyList()
            }

            val jsonObject = JSONObject(response.body())
            val dataArray = jsonObject.getJSONArray("data")
            val prices = mutableListOf<PricePoint>()
            for (i in 0 until dataArray.length()) {
                val obj = dataArray.getJSONObject(i)
                val dtStr = obj.getString("date")
                val values = obj.getJSONObject("values")
                val tariff = values.getDouble("allInPrijs") // Using all-in price
                prices.add(PricePoint(OffsetDateTime.parse(dtStr).toInstant(), tariff))
            }
            prices
        } catch (e: Exception) {
            println("[ERROR] Exception during price fetch: ${e.message}")
            emptyList()
        }
    }

    fun getCheapestHours(prices: List<PricePoint>, count: Int): Set<Instant> {
        return prices.sortedBy { it.tariff }
            .take(count)
            .map { it.dateTime }
            .toSet()
    }
}
