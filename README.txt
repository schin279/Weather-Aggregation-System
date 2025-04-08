Prerequisites:
    - Java Development Kit (JDK) 8 or higher
    - Google Gson library (gson-2.10.1.jar)

Installation:
    1. Unzip the source files into an empty folder
    2. Compile all Java files:
        javac -cp ".;path/to/gson-2.10.1.jar" AggregationServer.java ContentServer.java GETClient.java JSONParser.java LamportClock.java WeatherData.java WeatherEntry.java

Usage:
    1. Start the Aggregation Server:
        java -cp ".;path/to/gson-2.10.1.jar" AggregationServer [port]
        
        Note: Default port is 4567 if not specified

    2. Send Weather Data (Content Server):
        java -cp ".;path/to/gson-2.10.1.jar" ContentServer <server_url> <weather_data_file>
        
        Example: java -cp ".;path/to/gson-2.10.1.jar" ContentServer http://localhost:4567 weather_data.txt

    3. Retrieving Weather Data (GET Client):
        java -cp ".;path/to/gson-2.10.1.jar" GETClient <server_url>

        Example: java -cp ".;path/to/gson-2.10.1.jar" GETClient http://localhost:4567

        For a specific station (OPTIONAL):
        java -cp ".;path/to/gson-2.10.1.jar" GETClient <server_url> <station_id>