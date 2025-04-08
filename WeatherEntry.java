public class WeatherEntry implements Comparable<WeatherEntry> {
    String id;
    long timestamp;

    WeatherEntry(String id, long timestamp) {
        this.id = id;
        this.timestamp = timestamp;
    }

    @Override
    public int compareTo(WeatherEntry other) {
        return Long.compare(this.timestamp, other.timestamp);
    }
}
