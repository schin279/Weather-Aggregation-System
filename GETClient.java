import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GETClient {
    private static LamportClock lamportClock = new LamportClock();

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java GETClient <server_url> [station_id]");
            return;
        }

        String serverUrl = args[0];
        String stationId = args.length > 1 ? args[1] : null;

        try {
            lamportClock.increment(); // Increment Lamport clock before sending request

            // Handle missing "http://" in the server URL
            if (!serverUrl.startsWith("http://")) {
                serverUrl = "http://" + serverUrl;
            }

            // Build the full URI based on whether a station ID is provided
            URI uri;
            if (stationId != null) {
                uri = new URI(serverUrl + "/weather.json?id=" + stationId);
            } else {
                uri = new URI(serverUrl + "/weather.json");
            }

            // Create the URL connection
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Lamport-Clock", String.valueOf(lamportClock.getClock()));
            connection.setConnectTimeout(5000); // Connection timeout
            connection.setReadTimeout(5000);    // Read timeout

            // Check the response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Update Lamport clock based on server response
                String responseClock = connection.getHeaderField("Lamport-Clock");
                if (responseClock != null) {
                    lamportClock.update(Integer.parseInt(responseClock));
                } else {
                    System.out.println("No Lamport-Clock header in the response.");
                }

                // Read the JSON response
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Parse and display the JSON data in a readable format
                parseAndDisplayWeatherData(response.toString());
            } else {
                System.out.println("Failed to retrieve data. Server response code: " + responseCode);
            }
        } catch (IOException e) {
            System.err.println("I/O error occurred: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
        }
    }

    // Method to parse and display the JSON weather data in a readable format
    private static void parseAndDisplayWeatherData(String jsonResponse) {
        try {
            JsonArray weatherDataArray = JsonParser.parseString(jsonResponse).getAsJsonArray();
            
            if (weatherDataArray.size() == 0) {
                System.out.println("No weather data available.");
                return;
            }

            System.out.println("Weather Data:");
            for (int i = 0; i < weatherDataArray.size(); i++) {
                JsonObject weatherData = weatherDataArray.get(i).getAsJsonObject();
                System.out.println("\nStation " + (i + 1) + ":");
                for (String key : weatherData.keySet()) {
                    JsonElement value = weatherData.get(key);
                    if (!value.isJsonNull()) {
                        System.out.println(key + ": " + value.getAsString());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing weather data: " + e.getMessage());
        }
    }
}