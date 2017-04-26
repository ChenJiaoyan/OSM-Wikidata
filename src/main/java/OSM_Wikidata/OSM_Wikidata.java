package OSM_Wikidata;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by SmallApple on 2017/4/17.
 */
public class OSM_Wikidata {
    static final String Context = "http://crowdgeokg.org/context.jsonld";
    static ArrayList<String> Type = null;
    static String ID = null;
    static String IDType = null;
    static String Name_zh = null;
    static String Name_en = null;
    static String WKT = null;
    static String URL = null;
    static HashMap<String, ArrayList<String>> SameAs = null;

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

    public void setSameAs(HashMap<String, ArrayList<String>> SameAs) {
        this.SameAs = SameAs;
    }

    public static String getContext() {
        return Context;
    }

    public static String getID() {
        return ID;
    }

    public static String getIDType() {
        return IDType;
    }

    public static ArrayList<String> getType() {
        return Type;
    }

    public static String getName_zh() {
        return Name_zh;
    }

    public static String getName_en() {
        return Name_en;
    }

    public static String getURL() {
        return URL;
    }

    public static String getWKT() {
        return WKT;
    }

    public static HashMap<String, ArrayList<String>> getSameAs() {
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
        ArrayList rdf = new ArrayList<String>();
        String context = "\"@context\": \"" + getContext() + "\"";
        String id = "\"@id\": \"/" + getIDType() + "/" + getID() + "\"";
        String type = "\"@type\": " + getType();
        String name = "\"name\": [\n" +
                "\r\n\r\t\r\t{\"@value\": \""+ getName_en() + "\", \"@language\": \"en\"},\r\n" +
                "\r\t\r\t{\"@value\": \""+ getName_zh() + "\", \"@language\": \"zh\"},\r\n" +
                "\r\t]";
        String wkt = "\"asWKT\": \"" + getWKT() + "\"";
        String url = "\"url\": " + getURL();
        String s1 = getSameAs().keySet().toString();
        String s2 = new String(s1.substring(1, s1.length()-1));
        String same = "\"sameAs\": {\n" +
                "\r\n\r\t\r\t\"@id\": \"" + s2 +  "\",\r\n" +
                "\r\t\r\t\"@type\": " + getSameAs().get(s2) +  ",\r\n" +
                "\r\t}";
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
