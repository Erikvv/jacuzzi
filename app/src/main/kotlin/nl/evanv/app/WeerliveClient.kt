package nl.evanv.app

import org.json.JSONObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import kotlin.math.min

/**
 * Client for fetching weather data from KNMI (via Weerlive API).
 */
open class WeerliveClient(
    private val apiKey: String = System.getenv("WEERLIVE_API_KEY"),
    private val location: String = "Amsterdam"
) {
    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    open fun fetchAverageTemperatureTomorrow(): Double {
        val url = "https://weerlive.nl/api/weerlive_api_v2.php?key=$apiKey&locatie=$location"
        val responseBody = performRequest(url)

        val json = JSONObject(responseBody)
        val dagData = json.getJSONArray("wk_verw")
            .getJSONObject(1)

        val minTemp = dagData.getDouble("min_temp")
        val maxTemp = dagData.getDouble("max_temp")

        println("[INFO] Temperature tomorrow is $minTemp to $maxTemp °C");

        return (minTemp + maxTemp) / 2
    }

    /**
     * Performs the underlying HTTP request. Protected to allow mocking in tests.
     */
    protected open fun performRequest(url: String): String {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Accept", "application/json")
            .GET()
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        
        if (response.statusCode() != 200) {
            throw RuntimeException("Failed to fetch weather data from KNMI: HTTP ${response.statusCode()} - ${response.body()}")
        }
        
        return response.body()
    }
}
