package OSM_Wikidata;

import FileHandle.HandleFiles;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.VCARD;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

import static OSM_Wikidata.OSM_Wikidata.getURI;

/**
 * Created by SmallApple on 2017/4/17.
 */
public class OSM extends OSM_Wikidata{
    String OSM_Type = "GeoEntity";
    String OSMType = null;
    public void setOSMType(String OSMType) {
        this.OSMType = OSMType;
    }
    String getOSMType() {
        return this.OSMType;
    }
    public static void RDF_OSM(String OSMwithWiki, String Wiki_NameEn, String RDFfile, String NodePath, String WayPath, String RelationPath) {
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
                osm.setOSMType(str[0].trim());
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
                    osm.setWKT(OSM2WKT.way2WKT(way, WayPath));
                }
                if(str[0].equals("relation")) {
                    Relations relation = new Relations();
                    relation = OSM2WKT.getRelationByID(str[1], RelationPath);
                    osm.setWKT(OSM2WKT.relation2WKT(relation, NodePath, WayPath, RelationPath));
                }
                osm.setURI("\"http://openstreetmap.org/" + str[0] + "/" + getID() + "\"");
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

    public static void RDFWriter_OSM(String OSMwithWiki, String Wiki_NameEn, String RDFfile, String NodeWikiPath, String NodePath, String WayPath, String RelationPath) {
        File file = new File(OSMwithWiki);
        BufferedReader reader = null;
        // create an empty model
        Model model = ModelFactory.createDefaultModel();
        // create the resource
        Resource rdf_osm;
        try {
            File rdfFile = new File(RDFfile);
            if (rdfFile.exists()) rdfFile.delete();
            rdfFile.createNewFile();
            reader = new BufferedReader(new FileReader(file), 10 * 1024 * 1024);
            String s = null;
            while ((s = reader.readLine()) != null) {
                String[] str = s.split(",");
                //为了去除运行过程中发现的字符串首部的奇怪字符
                if(str[0].indexOf("node") >= 0) {
                    str[0] = "node";
                }
                if(str[0].indexOf("way") >= 0) {
                    str[0] = "way";
                }
                if(str[0].indexOf("relation") >= 0) {
                    str[0] = "relation";
                }
                OSM osm = new OSM();
                osm.setIDType("osm");
                ArrayList type = new ArrayList();
                //type.add("\"OSMEntity\"");
                //type.add("\"OSM" + toUpperCaseFirstOne(str[0]) + "\"");
                type.add("OSMEntity");
                type.add("OSM" + toUpperCaseFirstOne(str[0]));
                osm.setType(type);
                osm.setID(str[1]);
                osm.setOSMType(str[0]);
                osm.setName_zh(str[2]);
                osm.setName_en(OSM2WKT.getEnName(str[3], Wiki_NameEn));
                HashMap<String, ArrayList<String>> wiki = new HashMap<String, ArrayList<String>>();
                ArrayList t = new ArrayList();
                //t.add("\"WikiDataEntity\"");
                t.add("WikidataEntity");
                wiki.put("/wikidata/" + str[3], t);
                osm.setSameAs(wiki);

                if(str[0].equals("node")) {
                    Nodes node = new Nodes();
                    node = OSM2WKT.getNodebyID(str[1], NodeWikiPath);
                    osm.setWKT(OSM2WKT.node2WKT(node));
                }

                if(str[0].equals("way")) {
                    Way way = new Way();
                    way = OSM2WKT.getWaybyID(str[1], WayPath);
                    osm.setWKT(OSM2WKT.way2WKT(way, NodePath));
                }


                if(str[0].equals("relation")) {
                    Relations relation = new Relations();
                    relation = OSM2WKT.getRelationByID(str[1], RelationPath);
                    osm.setWKT(OSM2WKT.relation2WKT(relation, NodePath, WayPath, RelationPath));
                }

                osm.setURI("http://openstreetmap.org/" + osm.getOSMType() + "/" + osm.getID());
                String stype = String.valueOf(wiki.values().toArray()[0]);
                String st = new String(stype.substring(1, stype.length()-1));

                // and add the properties cascading style
                String wkt = osm.getWKT();
                if(osm.getWKT() != null) {
                    if (!osm.getName_en().equals("No English Name")) {
                        rdf_osm = model.createResource(osm.getURI())
                                .addProperty(OWL.sameAs,
                                        model.createResource()
                                                .addProperty(RDF.type, st)
                                                .addProperty(VCARD.UID, String.valueOf(wiki.keySet().toArray()[0]))
                                )
                                .addProperty(VCARD.NAME,
                                        model.createResource()
                                                .addProperty(RDFS.label, model.createLiteral(osm.getName_en(), "en"))
                                                .addProperty(RDFS.label, model.createLiteral(osm.getName_zh(), "zh"))
                                )
                                .addProperty(VCARD.GEO, wkt)
                                .addProperty(VCARD.CLASS, osm.getType().get(1))
                                .addProperty(RDFS.subClassOf, osm.getType().get(0))
                                .addProperty(RDF.type, osm.OSM_Type)
                                .addProperty(VCARD.UID, osm.getIDType() + "/" + osm.getID())
                        ;
                    } else {
                        rdf_osm = model.createResource(osm.getURI())
                                .addProperty(OWL.sameAs,
                                        model.createResource()
                                                .addProperty(RDF.type, st)
                                                .addProperty(VCARD.UID, String.valueOf(wiki.keySet().toArray()[0]))
                                )
                                .addProperty(VCARD.NAME, osm.getName_zh())
                                .addProperty(VCARD.GEO, wkt)
                                .addProperty(VCARD.CLASS, osm.getType().get(1))
                                .addProperty(RDFS.subClassOf, osm.getType().get(0))
                                .addProperty(RDF.type, osm.OSM_Type)
                                .addProperty(VCARD.UID, osm.getIDType() + "/" + osm.getID())
                        ;
                    }
                }
                model.write(System.out, "RDF/XML-ABBREV");
                //model.write(System.out, "N-TRIPLES");
                System.out.println(s);
                FileWriter out = new FileWriter(RDFfile);
                model.write( out, "RDF/XML-ABBREV" );
            }
            reader.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        /*
        FileWriter out = null;
        try {
            out = new FileWriter(RDFfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            model.write( out, "RDF/XML-ABBREV" );
            //model.write(out, "N-TRIPLES");
        }
        finally {
            try {
                out.close();
            }
            catch (IOException closeException) {
                // ignore
            }
        }
        */
        // now write the model in XML form to a file
        /*model.write(System.out, "RDF/XML-ABBREV");
        //model.write(System.out, "N-TRIPLES");
        System.out.println("\n");*/
        /*ByteArrayOutputStream sout = new ByteArrayOutputStream();
        model.write(sout, "RDF/XML-ABBREV");
        //model.write(sout, "N-TRIPLES");
        HandleFiles.WriteFile(RDFfile, sout.toString());*/
    }

    public static void main(String[] args) {
        // node2RDF
        String Wiki_NameEn = "F:\\Wiki-Name_EN&&ID.csv";
        //-Addition

        String OSMwithWiki_Taiwan = "F:\\OSMwithWiki_Taiwan.csv";
        String OSMwithWiki_China = "F:\\OSMwithWiki_China.csv";
        String RDFfile_Taiwan = "F:\\RDF_OSM_Taiwan.ttl";
        String RDFfile_China = "F:\\RDF_OSM_China.ttl";
        //String RDFfile_Taiwan = "F:\\RDF_OSM_Taiwan.n3";
        //String RDFfile_China = "F:\\RDF_OSM_China.n3";
        String NodePath_Taiwan = "F:\\NodePath_Taiwan.txt";
        String NodeWikiPath_Taiwan = "F:\\NodePath(Wiki)_Taiwan.txt";
        String WayPath_Taiwan = "F:\\WayPath_Taiwan.txt";
        String RelationPath_Taiwan = "F:\\RelationPath_Taiwan.txt";
        String NodeWikiPath_China = "F:\\NodePath(Wiki)_China.txt";
        String NodePath_China = "F:\\NodePath_China.txt";
        String WayPath_China = "F:\\WayPath_China.txt";
        String RelationPath_China = "F:\\RelationPath_China.txt";

        RDF_OSM(OSMwithWiki_Taiwan, Wiki_NameEn, RDFfile_Taiwan, NodePath_Taiwan, WayPath_Taiwan, RelationPath_Taiwan);
        RDF_OSM(OSMwithWiki_China, Wiki_NameEn, RDFfile_China, NodePath_China, WayPath_China, RelationPath_China);
        //RDFWriter_OSM(OSMwithWiki_Taiwan, Wiki_NameEn, RDFfile_Taiwan, NodeWikiPath_Taiwan, NodePath_Taiwan, WayPath_Taiwan, RelationPath_Taiwan);
        //RDFWriter_OSM(OSMwithWiki_China, Wiki_NameEn, RDFfile_China, NodeWikiPath_China, NodePath_China, WayPath_China, RelationPath_China);

        /*
        //String fileS = "F:\\Test\\NodePath(Wiki)_China2.txt";
        String fileS = "F:\\Test\\OSMwithWiki_China.csv";
        try {
            HandleFiles.splitFile(fileS);
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
        /*
        String OSMwithWiki_China = "F:\\Test\\OSMwithWiki_China_30.csv";
        String RDFfile_China = "F:\\Test\\RDF_OSM_China_30.ttl";
        String NodeWikiPath_China = "F:\\Test\\NodePath(Wiki)_China2_30.txt";
        String NodePath_China = "F:\\NodePath_China.txt";
        String WayPath_China = "F:\\WayPath_China.txt";
        String RelationPath_China = "F:\\RelationPath_China.txt";
        RDFWriter_OSM(OSMwithWiki_China, Wiki_NameEn, RDFfile_China, NodeWikiPath_China, NodePath_China, WayPath_China, RelationPath_China);
    */
    }
}
