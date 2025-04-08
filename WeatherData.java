import java.io.Serializable;
import java.util.Objects;  // Import Objects class for hashCode and equals


public class WeatherData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String name;
    private String state;
    private String timeZone;
    private double lat;
    private double lon;
    private String localDateTime;
    private String localDateTimeFull;
    private double airTemp;
    private double apparentTemp;
    private String cloud;
    private double dewPoint;
    private double pressure;
    private int relHum;
    private String windDir;
    private int windSpeedKmh;
    private int windSpeedKt;

    // No-argument constructor
    public WeatherData() {
        // Can be used to initialise the object and set properties later
    }

    // Constructor with validation
    public WeatherData(String id, String name, String state, String timeZone, double lat, double lon,
                       String localDateTime, String localDateTimeFull, double airTemp, double apparentTemp,
                       String cloud, double dewPoint, double pressure, int relHum, String windDir, int windSpeedKmh, int windSpeedKt) {
        
        if(lat < -90 || lat > 90 || lon < -180 || lon > 180) {
            throw new IllegalArgumentException("Invalid latitude or longitude");
        }
        if (relHum< 0 || relHum > 100) {
            throw new IllegalArgumentException("Humidity should be between 0 and 100");
        }

        this.id = id;
        this.name = name;
        this.state = state;
        this.timeZone = timeZone;
        this.lat = lat;
        this.lon = lon;
        this.localDateTime = localDateTime;
        this.localDateTimeFull = localDateTimeFull;
        this.airTemp = airTemp;
        this.apparentTemp = apparentTemp;
        this.cloud = cloud;
        this.dewPoint = dewPoint;
        this.pressure = pressure;
        this.relHum = relHum;
        this.windDir = windDir;
        this.windSpeedKmh = windSpeedKmh;
        this.windSpeedKt = windSpeedKt;
    }

    // Getters and Setters for each field
    public String getId() { return this.id; }
    public String getName() { return this.name; }
    public String getState() { return this.state; }
    public String getTimeZone() { return this.timeZone; }
    public double getLat() { return this.lat; }
    public double getLon() { return this.lon; }
    public String getLocalDateTime() { return this.localDateTime; }
    public String getLocalDateTimeFull() { return this.localDateTimeFull; }
    public double getAirTemp() { return this.airTemp; }
    public double getApparentTemp() { return this.apparentTemp; }
    public String getCloud() { return this.cloud; }
    public double getDewPoint() { return this.dewPoint; }
    public double getPressure() { return this.pressure; }
    public int getRelHum() { return this.relHum; }
    public String getWindDir() { return this.windDir; }
    public int getWindSpeedKmh() { return this.windSpeedKmh; }
    public int getWindSpeedKt() { return this.windSpeedKt; }
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setState(String state) { this.state = state; }
    public void setTimeZone(String timeZone) { this.timeZone = timeZone; }
    public void setLat(double lat) { this.lat = lat; }
    public void setLon(double lon) { this.lon = lon; }
    public void setLocalDateTime(String localDateTime) { this.localDateTime = localDateTime; }
    public void setLocalDateTimeFull(String localDateTimeFull) { this.localDateTimeFull = localDateTimeFull; }
    public void setAirTemp(double airTemp) { this.airTemp = airTemp; }
    public void setApparentTemp(double apparentTemp) { this.apparentTemp = apparentTemp; }
    public void setCloud(String cloud) { this.cloud = cloud; }
    public void setDewPoint(double dewPoint) { this.dewPoint = dewPoint; }
    public void setPressure(double pressure) { this.pressure = pressure; }
    public void setRelHum(int relHum) { this.relHum = relHum; }
    public void setWindDir(String windDir) { this.windDir = windDir; }
    public void setWindSpdKmh(int windSpeedKmh) { this.windSpeedKmh = windSpeedKmh; }
    public void setWindSpdKt(int windSpeedKt) { this.windSpeedKt = windSpeedKt; }

    // Override toString method for readability
    @Override
    public String toString() {
        return "WeatherData{id='" + id + "', name='" + name + "', state='" + state + "', airTemp=" + airTemp + 
               ", apparentTemp=" + apparentTemp + ", rel_hum=" + relHum + ", windSpeedKmh=" + windSpeedKmh + "}";
    }

    // Override equals() and hashCode() for comparison
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeatherData that = (WeatherData) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}