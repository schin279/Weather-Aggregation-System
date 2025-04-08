# Weather Feed Aggregation System with Lamport Clocks

This project implements a weather data aggregation system in Java using **sockets**, incorporating **Lamport clocks** for synchronisation and coordination across distributed components.

## Project Overview

The system is designed to:

- Aggregate weather data from multiple content servers.
- Serve consistent, Lamport-clock synchronised feeds to GET clients.
- Handle failures (client, server, and network).
- Expire data from inactive content servers after 30 seconds.
- Support Lamport clocks across all entities for event ordering.

## Components

### 1. **Aggregation Server**
- Accepts `PUT` requests from Content Servers and `GET` requests from Clients.
- Maintains data in a recoverable format (not directly overwritten).
- Ensures atomicity and persistence (restores data post-crash).
- Serializes concurrent `PUT` operations using Lamport clock timestamps.
- Removes data from content servers that are inactive for 30+ seconds.
- Handles:
  - `PUT`: Accepts JSON-formatted weather data.
  - `GET`: Responds with aggregated weather feed in JSON.
  - Other methods: Responds with HTTP 400.
- Implements Lamport Clock synchronisation.

### 2. **Content Server**
- Reads local input file with weather data.
- Converts it to JSON format and sends `PUT` to the Aggregation Server.
- Implements Lamport clock tagging and updates.
- Handles acknowledgement status codes:
  - `201`: First successful upload.
  - `200`: Subsequent successful updates.
  - `204`: No content.
  - `500`: Malformed or invalid data.

### 3. **GET Client**
- Sends `GET` requests to the Aggregation Server.
- Displays parsed JSON data in a human-readable format.
- Accepts optional station ID.
- Implements Lamport clock tagging and updates.
- Handles malformed responses and disconnections.

## Lamport Clock Support
All entities (Aggregation Server, Content Server, and GET Client) maintain and update Lamport clocks with each event:
- On message send: Increment clock and attach to message.
- On message receive: Update clock using `max(local, received) + 1`.
- Clocks are embedded in headers or as metadata in messages.

## Prerequisites
- Java Development Kit (JDK) 8 or higher  
- [Google Gson library](https://github.com/google/gson) (`gson-2.10.1.jar`)

## Installation
1. Unzip the source files into an empty folder.  
2. Compile all Java files:
```bash
javac -cp ".;path/to/gson-2.10.1.jar" AggregationServer.java ContentServer.java GETClient.java JSONParser.java LamportClock.java WeatherData.java WeatherEntry.java
```

## Usage
### 1. Start the Aggregation Server
```bash
java -cp ".;path/to/gson-2.10.1.jar" AggregationServer [port]
```
### 2. Send Weather Data (Content Server)
```bash
java -cp ".;path/to/gson-2.10.1.jar" ContentServer <server_url> <weather_data_file>
```
### 3. Retrieve Weather Data (GET Client)
```bash
java -cp ".;path/to/gson-2.10.1.jar" GETClient <server_url>
```
