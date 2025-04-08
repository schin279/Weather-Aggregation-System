import java.io.*;
import java.net.*;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class ContentServer {
    private static LamportClock lamportClock = new LamportClock();  // Assuming LamportClock is defined elsewhere

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java ContentServer <server_url> <file_path>");
            return;
        }

        String serverUrl = args[0];
        String filePath = args[1];

        try {
            // Convert the file to JSON
            String json = convertTextFileToJson(filePath);
            if (json == null) {
                System.out.println("Error: Missing 'id' field in input file. Aborting.");
                return;
            }

            // Send the JSON data to the server
            sendPutRequest(serverUrl, json);
        } catch (IOException e) {
            System.out.println("Error reading the file: " + e.getMessage());
        } catch (JsonSyntaxException e) {
            System.out.println("Error parsing weather data from JSON: " + e.getMessage());
        }
    }

    // Method to convert the text file to a JSON string
    public static String convertTextFileToJson(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        WeatherData weatherData = new WeatherData();
        String line;

        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(":", 2); // Split only on the first ':'
            if (parts.length == 2) {
                String key = parts[0].trim();
                String value = parts[1].trim();
                
                // Map the parsed values to the WeatherData object
                switch (key) {
                    case "id":
                        weatherData.setId(value);
                        break;
                    case "name":
                        weatherData.setName(value);
                        break;
                    case "state":
                        weatherData.setState(value);
                        break;
                    case "time_zone":
                        weatherData.setTimeZone(value);
                        break;
                    case "lat":
                        weatherData.setLat(Double.parseDouble(value));
                        break;
                    case "lon":
                        weatherData.setLon(Double.parseDouble(value));
                        break;
                    case "local_date_time":
                        weatherData.setLocalDateTime(value);
                        break;
                    case "local_date_time_full":
                        weatherData.setLocalDateTimeFull(value);
                        break;
                    case "air_temp":
                        weatherData.setAirTemp(Double.parseDouble(value));
                        break;
                    case "apparent_t":
                        weatherData.setApparentTemp(Double.parseDouble(value));
                        break;
                    case "cloud":
                        weatherData.setCloud(value);
                        break;
                    case "dewpt":
                        weatherData.setDewPoint(Double.parseDouble(value));
                        break;
                    case "press":
                        weatherData.setPressure(Double.parseDouble(value));
                        break;
                    case "rel_hum":
                        weatherData.setRelHum(Integer.parseInt(value));
                        break;
                    case "wind_dir":
                        weatherData.setWindDir(value);
                        break;
                    case "wind_spd_kmh":
                        weatherData.setWindSpdKmh(Integer.parseInt(value));
                        break;
                    case "wind_spd_kt":
                        weatherData.setWindSpdKt(Integer.parseInt(value));
                        break;
                    default:
                        System.out.println("Unknown key: " + key);
                        break;
                }
            }
        }
        reader.close();

        // Check if 'id' is missing
        if (weatherData.getId() == null) {
            return null;
        }
 
        // Convert the WeatherData object to JSON
        Gson gson = new Gson();
        return gson.toJson(weatherData);
    }

    // Method to send a PUT request using a socket connection
    private static void sendPutRequest(String serverUrl, String jsonData) {
        Socket socket = null;
        try {
            // Parse the server URL
            URI uri = new URI(serverUrl);
            String host = uri.getHost();
            int port = uri.getPort() == -1 ? 80 : uri.getPort();
    
            // Establish a socket connection
            socket = new Socket(host, port);
            socket.setSoTimeout(30000); // 30 second timeout
            
            OutputStream out = socket.getOutputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // Increment Lamport Clock
            lamportClock.increment();
            int lamportTime = lamportClock.getClock();
    
            // Build the PUT request message
            String request = String.format("PUT /weather.json HTTP/1.1\r\n" +
                                         "Host: %s\r\n" +
                                         "User-Agent: ATOMClient/1/0\r\n" +
                                         "Content-Type: application/json\r\n" +
                                         "Lamport-Clock: %d\r\n" +
                                         "Content-Length: %d\r\n" +
                                         "\r\n" +
                                         "%s",
                                         host,
                                         lamportTime,
                                         jsonData.length(),
                                         jsonData);
            
            // Send the PUT request
            out.write(request.getBytes());
            out.flush();
    
            // Read the response status line
            String statusLine = in.readLine();
            System.out.println("Response: " + statusLine);
    
            // Read headers
            while ((request = in.readLine()) != null && !request.isEmpty()) {
                System.out.println(request);
            }
    
        } catch (Exception e) {
            System.out.println("Error sending PUT request: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
