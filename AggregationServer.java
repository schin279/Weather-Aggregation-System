import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

public class AggregationServer {
    private static final int EXPIRY_TIME = 30000; // Weather data expires after 30 seconds
    private static final Map<String, WeatherData> weatherDataMap = new ConcurrentHashMap<>();
    private static final Map<String, Long> serverTimestamps = new ConcurrentHashMap<>();
    private static final PriorityBlockingQueue<WeatherEntry> expiryQueue = new PriorityBlockingQueue<>();
    private static LamportClock lamportClock = new LamportClock();
    private static final String PERSISTENCE_FILE = "weatherData.dat"; // Path to store data for persistence

    public static void main(String[] args) {
        int port = 4567; // Default port
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        // Load persisted weather data on startup
        loadPersistedData();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Aggregation Server is running on port " + port);

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    new ClientHandler(clientSocket).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load persisted data from the file
    @SuppressWarnings("unchecked")
    private static void loadPersistedData() {
        File file = new File(PERSISTENCE_FILE);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Map<String, WeatherData> savedData = (Map<String, WeatherData>) ois.readObject();
            Map<String, Long> savedTimestamps = (Map<String, Long>) ois.readObject();

            weatherDataMap.putAll(savedData);
            serverTimestamps.putAll(savedTimestamps);

            // Add all entries to the expiry queue
            for (Map.Entry<String, Long> entry : savedTimestamps.entrySet()) {
                expiryQueue.add(new WeatherEntry(entry.getKey(), entry.getValue()));
            }

            System.out.println("Loaded persisted data.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to load persisted data.");
        }
    }

    // Persist data to file (Atomic File Replacement)
    private static synchronized void persistData() {
        File tempFile = new File(PERSISTENCE_FILE + ".tmp");
        File actualFile = new File(PERSISTENCE_FILE);
        
        try {
            // Write to temp file directly without checking parent directory
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tempFile))) {
                oos.writeObject(new HashMap<>(weatherDataMap));
                oos.writeObject(new HashMap<>(serverTimestamps));
                oos.flush();
            }
            
            // Atomic replacement
            if (actualFile.exists()) {
                actualFile.delete();
            }
            
            if (tempFile.renameTo(actualFile)) {
                System.out.println("Weather data persisted successfully.");
            } else {
                System.err.println("Failed to rename temp file to actual file.");
                // If rename fails, try to copy the contents
                try (FileInputStream fis = new FileInputStream(tempFile);
                     FileOutputStream fos = new FileOutputStream(actualFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                    System.out.println("Weather data persisted using copy method.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to persist data: " + e.getMessage());
        } finally {
            // Clean up temp file if it still exists
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    // ClientHandler: Handles client connections
    private static class ClientHandler extends Thread {
        private final Socket clientSocket;
    
        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }
    
        @Override
        public void run() {
            try (
                InputStream input = clientSocket.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(input));
                OutputStream output = clientSocket.getOutputStream();
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(output))
            ) {
                String inputLine = in.readLine();
                if (inputLine == null) {
                    return;
                }
    
                String[] requestParts = inputLine.split(" ");
                if (requestParts.length < 2) {
                    sendErrorResponse(out, "HTTP/1.1 400 Bad Request");
                    return;
                }
    
                String method = requestParts[0];
                String resource = requestParts[1];
    
                // Read headers
                String lamportHeader = null;
                int contentLength = 0;
                String line;
                while ((line = in.readLine()) != null && !line.isEmpty()) {
                    if (line.startsWith("Lamport-Clock:")) {
                        lamportHeader = line.split(": ")[1];
                    } else if (line.startsWith("Content-Length:")) {
                        contentLength = Integer.parseInt(line.split(": ")[1].trim());
                    }
                }
    
                if (lamportHeader != null) {
                    lamportClock.update(Integer.parseInt(lamportHeader));
                }
    
                if ("PUT".equalsIgnoreCase(method) && "/weather.json".equalsIgnoreCase(resource)) {
                    handlePutRequest(in, out, contentLength);
                } else if ("GET".equalsIgnoreCase(method) && "/weather.json".equalsIgnoreCase(resource)) {
                    handleGetRequest(out);
                } else {
                    sendErrorResponse(out, "HTTP/1.1 400 Bad Request");
                }
    
            } catch (Exception e) {
                e.printStackTrace();
                try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {
                    sendErrorResponse(out, "HTTP/1.1 500 Internal Server Error");
                } catch (IOException ignored) {
                    // Ignore close exception
                }
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private static void sendErrorResponse(BufferedWriter out, String statusLine) throws IOException {
            lamportClock.increment();
            out.write(statusLine + "\r\n");
            out.write("Lamport-Clock: " + lamportClock.getClock() + "\r\n");
            out.write("\r\n");
            out.flush();
        }

        private static void handlePutRequest(BufferedReader in, BufferedWriter out, int contentLength) throws IOException {
            if (contentLength == 0) {
                sendErrorResponse(out, "HTTP/1.1 204 No Content");
                return;
            }
    
            // Read the body based on Content-Length
            char[] body = new char[contentLength];
            int bytesRead = in.read(body, 0, contentLength);
            if (bytesRead != contentLength) {
                sendErrorResponse(out, "HTTP/1.1 400 Bad Request");
                return;
            }
    
            try {
                WeatherData data = JSONParser.parseWeatherData(new String(body));
                
                if (isValidWeatherData(data)) {
                    boolean isNewEntry = !weatherDataMap.containsKey(data.getId());
                    weatherDataMap.put(data.getId(), data);
                    serverTimestamps.put(data.getId(), System.currentTimeMillis());
                    expiryQueue.add(new WeatherEntry(data.getId(), System.currentTimeMillis()));
    
                    // Persist the data on every successful PUT
                    persistData();
                    
                    lamportClock.increment();
                    String status = isNewEntry ? "HTTP/1.1 201 Created" : "HTTP/1.1 200 OK";
                    out.write(status + "\r\n");
                    out.write("Lamport-Clock: " + lamportClock.getClock() + "\r\n");
                    out.write("\r\n");
                    out.flush();
                } else {
                    sendErrorResponse(out, "HTTP/1.1 400 Bad Request");
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendErrorResponse(out, "HTTP/1.1 500 Internal Server Error");
            }
        }

        private static boolean isValidWeatherData(WeatherData data) {
            return data.getId() != null;
        }

        private static void handleGetRequest(BufferedWriter out) throws IOException {
            lamportClock.increment();

            removeExpiredEntries();

            List<WeatherData> recentData = new ArrayList<>(weatherDataMap.values());

            String jsonResponse = "[\n";
            for (int i = 0; i < recentData.size(); i++) {
                jsonResponse += JSONParser.convertToJSON(recentData.get(i));
                if (i < recentData.size() - 1) jsonResponse += ",";
            }
            jsonResponse += "]";

            out.write("HTTP/1.1 200 OK\r\n");
            out.write("Content-Type: application/json\r\n");
            out.write("Lamport-Clock: " + lamportClock.getClock() + "\r\n");
            out.write("\r\n");
            out.write(jsonResponse);
        }

        private static void removeExpiredEntries() {
            long currentTime = System.currentTimeMillis();

            while (!expiryQueue.isEmpty() && currentTime - expiryQueue.peek().timestamp > EXPIRY_TIME) {
                WeatherEntry entry = expiryQueue.poll();
                weatherDataMap.remove(entry.id);
                serverTimestamps.remove(entry.id);
            }

            // Persist updated data after removal of expired entries
            persistData();
        }
    }
}