package OSM_Wikidata;

import FileHandle.HandleFiles;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by SmallApple on 2017/4/17.
 */
public class Wikidata extends OSM_Wikidata{
    private static String NodePath = "F:\\NodePath.txt";
    private static String WayPath = "F:\\WayPath.txt";
    private static String RelationPath = "F:\\RelationPath.txt";
    public static void RDF_Wiki(String OSMwithWiki, String Wiki_NameEn, String RDFfile) {
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
                Wikidata wiki = new Wikidata();
                wiki.setIDType("wikidata");
                ArrayList type = new ArrayList();
                type.add("\"WikiDataEntity\"");
                wiki.setType(type);
                wiki.setID(str[3]);
                wiki.setName_zh(str[2]);
                wiki.setName_en(OSM2WKT.getEnName(str[3], Wiki_NameEn));
                ArrayList t = new ArrayList();
                t.add("\"OSMEntity\"");
                t.add("\"OSM" + toUpperCaseFirstOne(str[0]) + "\"");
                HashMap osm = new HashMap();
                osm.put("/osm/" + str[0] + "/" + str[1], t);
                wiki.setSameAs(osm);
                if(str[0].equals("node")) {
                    Nodes node = new Nodes();
                    node = OSM2WKT.getNodebyID(str[1], NodePath);
                    wiki.setWKT(OSM2WKT.node2WKT(node));
                }
                if(str[0].equals("way")) {
                    Way way = new Way();
                    way = OSM2WKT.getWaybyID(str[1], WayPath);
                    wiki.setWKT(OSM2WKT.way2WKT(way));
                }
                if(str[0].equals("relation")) {
                    Relations relation = new Relations();
                    relation = OSM2WKT.getRelationByID(str[1], RelationPath);
                    wiki.setWKT(OSM2WKT.relation2WKT(relation));
                }
                wiki.setURL("\"http://www.wikidata.org/wiki/" + getID() + "\"");
                ArrayList rdf = new ArrayList();
                rdf = RDF_Generate();
                System.out.println("{");
                HandleFiles.WriteFile(RDFfile, "{\r\n");
                for(int i = 0; i < rdf.size(); i++) {
                    System.out.println("\t" + rdf.get(i) + ",");
                    HandleFiles.WriteFile(RDFfile, "\r\t" + (String) rdf.get(i) + ",\r\n");
                }
                System.out.println("}");
                HandleFiles.WriteFile(RDFfile, "}\r\n");
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
        String RDFfile_Taiwan = "F:\\RDF_Wiki_Taiwan.ttl";
        String RDFfile_China = "F:\\RDF_Wiki_China.ttl";
        RDF_Wiki(OSMwithWiki_Taiwan, Wiki_NameEn, RDFfile_Taiwan);
        RDF_Wiki(OSMwithWiki_China, Wiki_NameEn, RDFfile_China);
    }

}
