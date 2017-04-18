package OSM_Wikidata;

import java.util.ArrayList;

/**
 * Created by SmallApple on 2017/4/17.
 */
public class OSM_Wikidata {
    final String Context = "http://crowdgeokg.org/context.jsonld";
    ArrayList<String> Type = null;
    String ID = null;
    String IDType = null;
    String Name_zh = null;
    String Name_en = null;
    String WKT = null;
    String URL = null;
    ArrayList<String> SameAs = null;

    public void setID(String ID) {
        this.ID = ID;
    }

    public void setIDType(String IDType) {
        this.IDType = IDType;
    }

    public void setType(ArrayList type) {
        Type = type;
    }

    public void setName_zh(String name_zh) {
        Name_zh = name_zh;
    }

    public void setName_en(String name_en) {
        Name_en = name_en;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public void setWKT(String WKT) {
        this.WKT = WKT;
    }

    public String getContext() {
        return Context;
    }

    public String getID() {
        return ID;
    }

    public String getIDType() {
        return IDType;
    }

    public ArrayList<String> getType() {
        return Type;
    }

    public String getName_zh() {
        return Name_zh;
    }

    public String getName_en() {
        return Name_en;
    }

    public String getURL() {
        return URL;
    }

    public String getWKT() {
        return WKT;
    }

    public ArrayList<String> getSameAs() {
        return SameAs;
    }

    public ArrayList RDF_Generate () {
        ArrayList rdf = new ArrayList<String>();
        String context = "\"@context\": \"" + getContext() + "\"";
        String id = "\"@id\": \"/" + getIDType() + "/" + getID() + "\"";
        String type = "\"@type\":" + getType();
        String name = "\"name\": [\n" +
                    "\t{\"@value\":\""+ getName_en() + "\", \"@language\":\"en\"},\n" +
                    "\t{\"@value\":\""+ getName_zh() + "\", \"@language\":\"zh\"},\n" +
                    "]";
        String wkt = "\"asWKT\": \"" + getWKT() + "\"";
        String url = getURL();
        String same = "\"sameAs\":{\n" +
                    "\t\"" + getSameAs().get(0) +  "\",\n" +
                    "\t\"" + getSameAs().get(1) +  "\",\n" +
                    "}";
        rdf.add(context);
        rdf.add(id);
        rdf.add(type);
        rdf.add(name);
        rdf.add(wkt);
        rdf.add(url);
        rdf.add(same);
        return rdf;
    }
}
