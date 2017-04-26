package OSM_Wikidata;

import FileHandle.HandleFiles;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by SmallApple on 2017/4/17.
 */
public class OSM extends OSM_Wikidata{
    private static String NodePath = "F:\\NodePath.txt";
    private static String WayPath = "F:\\WayPath.txt";
    private static String RelationPath = "F:\\RelationPath.txt";
    String OSMType = null;
    public void setOSMType(String OSMType) {
        this.OSMType = OSMType;
    }
    String getOSMType() {
        return this.OSMType;
    }
    public static void RDF_OSM(String OSMwithWiki, String Wiki_NameEn, String RDFfile) {
        File file = new File(OSMwithWiki);
        BufferedReader reader = null;
        try {
            File rdfFile = new File(RDFfile);
            if (rdfFile.exists()) rdfFile.delete();
            rdfFile.createNewFile();
            reader = new BufferedReader(new FileReader(file), 10 * 1024 * 1024);
            String s = null;
            while ((s = reader.readLine()) != null) {
                String[] str = s.split(",");
                OSM osm = new OSM();
                osm.setIDType("osm");
                ArrayList type = new ArrayList();
                type.add("\"OSMEntity\"");
                type.add("\"OSM" + toUpperCaseFirstOne(str[0]) + "\"");
                osm.setType(type);
                osm.setID(str[1]);
                osm.setOSMType(str[0]);
                osm.setName_zh(str[2]);
                osm.setName_en(OSM2WKT.getEnName(str[3], Wiki_NameEn));
                HashMap<String, ArrayList<String>> wiki = new HashMap<String, ArrayList<String>>();
                ArrayList t = new ArrayList();
                t.add("\"WikiDataEntity\"");
                wiki.put("/wikidata/" + str[3], t);
                osm.setSameAs(wiki);
                if(str[0].equals("node")) {
                    Nodes node = new Nodes();
                    node = OSM2WKT.getNodebyID(str[1], NodePath);
                    osm.setWKT(OSM2WKT.node2WKT(node));
                }
                if(str[0].equals("way")) {
                    Way way = new Way();
                    way = OSM2WKT.getWaybyID(str[1], WayPath);
                    osm.setWKT(OSM2WKT.way2WKT(way));
                }
                if(str[0].equals("relation")) {
                    Relations relation = new Relations();
                    relation = OSM2WKT.getRelationByID(str[1], RelationPath);
                    osm.setWKT(OSM2WKT.relation2WKT(relation));
                }
                osm.setURL("\"http://openstreetmap.org/" + str[0] + "/" + getID() + "\"");
                ArrayList rdf = new ArrayList();
                rdf = RDF_Generate();
                System.out.println("{\n\"@graph\": [");
                HandleFiles.WriteFile(RDFfile, "{\r\n\"@graph\": [\r\n");
                for(int i = 0; i < rdf.size(); i++) {
                    System.out.println("\t" + rdf.get(i) + ",");
                    HandleFiles.WriteFile(RDFfile, "\r\t" + (String) rdf.get(i) + ",\r\n");
                }
                System.out.println("]\n}");
                HandleFiles.WriteFile(RDFfile, "]\r\n}\r\n");
            }
            reader.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // node2RDF
        String Wiki_NameEn = "F:\\Wiki-Name_EN&&ID.csv";
        String OSMwithWiki_Taiwan = "F:\\OSMwithWiki_Taiwan.csv";
        String OSMwithWiki_China = "F:\\OSMwithWiki_China.csv";
        String RDFfile_Taiwan = "F:\\RDF_OSM_Taiwan.ttl";
        String RDFfile_China = "F:\\RDF_OSM_China.ttl";
        RDF_OSM(OSMwithWiki_Taiwan, Wiki_NameEn, RDFfile_Taiwan);
        RDF_OSM(OSMwithWiki_China, Wiki_NameEn, RDFfile_China);
    }
}
