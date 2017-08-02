package OSM_Wikidata;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by SmallApple on 2017/4/17.
 */
public class OSM_Wikidata {
    static final String Context = "http://crowdgeokg.org/context.jsonld";
    private ArrayList<String> Type = null;
    private String ID = null;
    private String IDType = null;
    private String Label = null;
    private String Name_zh = null;
    private String Name_en = null;
    private String WKT = null;
    private String URI = null;
    //private HashMap<String, ArrayList<String>> SameAs = null;
    private String SameAs = null;

    public void setID(String ID) {
        this.ID = ID;
    }

    public void setIDType(String IDType) {
        this.IDType = IDType;
    }

    public void setType(ArrayList type) {
        Type = type;
    }

    public void setLabel(String label) {
        Label = label;
    }

    public void setName_zh(String name_zh) {
        Name_zh = name_zh;
    }

    public void setName_en(String name_en) {
        Name_en = name_en;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }

    public void setWKT(String WKT) {
        this.WKT = WKT;
    }

    /*
    public void setSameAs(HashMap<String, ArrayList<String>> SameAs) {
        this.SameAs = SameAs;
    }
    */
    public void setSameAs(String SameAs) {
        this.SameAs = SameAs;
    }

    public static String getContext() {
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

    public String getLabel() {
        return Label;
    }

    public String getName_zh() {
        return Name_zh;
    }

    public String getName_en() {
        return Name_en;
    }

    public String getURI() {
        return URI;
    }

    public String getWKT() {
        return WKT;
    }

    /*
    public HashMap<String, ArrayList<String>> getSameAs() {
        return SameAs;
    }
    */
    public String getSameAs() {
        return SameAs;
    }

    //首字母转小写
    public static String toLowerCaseFirstOne(String s){
        if(Character.isLowerCase(s.charAt(0)))
            return s;
        else
            return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
    }
    //首字母转大写
    public static String toUpperCaseFirstOne(String s){
        if(Character.isUpperCase(s.charAt(0)))
            return s;
        else
            return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
    }

    public static ArrayList RDF_Generate() {
        OSM_Wikidata OW = new OSM_Wikidata();
        ArrayList rdf = new ArrayList<String>();
        String context = "\"@context\": \"" + getContext() + "\"";
        String id = "\"@id\": \"/" + OW.getIDType() + "/" + OW.getID() + "\"";
        String type = "\"@type\": " + OW.getType();
        String name = "\"name\": [\n" +
                "\r\n\r\t\r\t{\"@value\": \""+ OW.getName_en() + "\", \"@language\": \"en\"},\r\n" +
                "\r\t\r\t{\"@value\": \""+ OW.getName_zh() + "\", \"@language\": \"zh\"},\r\n" +
                "\r\t]";
        String wkt = "\"asWKT\": \"" + OW.getWKT() + "\"";
        String uri = "\"uri\": " + OW.getURI();
        /*
        String s1 = getSameAs().keySet().toString();
        String s2 = new String(s1.substring(1, s1.length()-1));
        String same = "\"sameAs\": {\n" +
                "\r\n\r\t\r\t\"@id\": \"" + s2 +  "\",\r\n" +
                "\r\t\r\t\"@type\": " + getSameAs().get(s2) +  ",\r\n" +
                "\r\t}";
        */
        rdf.add(context);
        rdf.add(id);
        rdf.add(type);
        rdf.add(name);
        rdf.add(wkt);
        rdf.add(uri);
        //rdf.add(same);
        return rdf;
    }
}
