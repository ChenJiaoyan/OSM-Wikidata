package OSM;

/**
 * Created by SmallApple on 2017/8/1.
 */
public class Nodes extends OSMEntity {

    private String lon;
    private String lat;

    public String getLon() {
        return lon;
    }
    public void setLon(String lon) {
        this.lon = lon;
    }
    public void setLat(String lat) {
        this.lat = lat;
    }
    public String getLat() {
        return lat;
    }

}
