package OSM_Wikidata;

/**
 * Created by SmallApple on 2017/4/17.
 */
public class OSM extends OSM_Wikidata{
    String OSMType = null;
    public void setOSMType(String OSMType) {
        this.OSMType = OSMType;
    }
    String getOSMType() {
        return this.OSMType;
    }

    public void main(String[] args) {
        setIDType("osm");
        setURL("\"http://openstreetmap.org/" + getOSMType() + "/" + getID() + "\"");
    }
}
