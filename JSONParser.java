import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class JSONParser {
    private static final Gson gson = new Gson();

    // Parses JSON into a WeatherData object with additional validation
    public static WeatherData parseWeatherData(String json) throws JsonSyntaxException {
        WeatherData data = gson.fromJson(json, WeatherData.class);
        // Validate critical fields after parsing
        if (data.getId() == null || data.getId().isEmpty()) {
            throw new IllegalArgumentException("Invalid WeatherData: ID is missing");
        }
        if (data.getLat() < -90 || data.getLat() > 90 || data.getLon() < -180 || data.getLon() > 180) {
            throw new IllegalArgumentException("Invalid latitude or longitude");
        }
        return data;
    }

    // Converts a WeatherData object into JSON with null safety check
    public static String convertToJSON(WeatherData data) {
        if (data == null) {
            throw new IllegalArgumentException("WeatherData object cannot be null");
        }
        return gson.toJson(data);
    }
}