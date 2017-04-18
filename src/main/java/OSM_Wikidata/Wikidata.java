package OSM_Wikidata;

/**
 * Created by SmallApple on 2017/4/17.
 */
public class Wikidata extends OSM_Wikidata{
    public void main(String[] args) {
        setIDType("wikidata");
        setURL("\"http://www.wikidata.org/wiki/" + getID() + "\"");
    }
}
