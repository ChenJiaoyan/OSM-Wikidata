package OSM_Wikidata;

import FileHandle.HandleFiles;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.VCARD;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by SmallApple on 2017/4/17.
 */
public class Wikidata extends OSM_Wikidata{
    public static void RDF_Wiki(String OSMwithWiki, String Wiki_NameEn, String RDFfile, String NodePath, String WayPath, String RelationPath) {
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
                    wiki.setWKT(OSM2WKT.way2WKT(way, WayPath));
                }
                if(str[0].equals("relation")) {
                    Relations relation = new Relations();
                    relation = OSM2WKT.getRelationByID(str[1], RelationPath);
                    wiki.setWKT(OSM2WKT.relation2WKT(relation, NodePath, WayPath, RelationPath));
                }
                wiki.setURI("\"http://www.wikidata.org/wiki/" + getID() + "\"");
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

    public static void RDFWriter_Wiki(String OSMwithWiki, String Wiki_NameEn, String RDFfile, String NodeWikiPath, String NodePath, String WayPath, String RelationPath) {
        File file = new File(OSMwithWiki);
        BufferedReader reader = null;
        // create an empty model
        Model model = ModelFactory.createDefaultModel();
        // create the resource
        Resource rdf_wiki;
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
                Wikidata wiki = new Wikidata();
                wiki.setIDType("wikidata");
                ArrayList type = new ArrayList();
                //type.add("\"WikiDataEntity\"");
                type.add("WikidataEntity");
                wiki.setType(type);
                wiki.setID(str[3]);
                wiki.setName_zh(str[2]);
                wiki.setName_en(OSM2WKT.getEnName(str[3], Wiki_NameEn));
                ArrayList t = new ArrayList();
                //t.add("\"OSMEntity\"");
                //t.add("\"OSM" + toUpperCaseFirstOne(str[0]) + "\"");
                t.add("OSMEntity");
                t.add("OSM" + toUpperCaseFirstOne(str[0]));
                HashMap osm = new HashMap();
                osm.put("/osm/" + str[0] + "/" + str[1], t);
                wiki.setSameAs(osm);

                if(str[0].equals("node")) {
                    Nodes node = new Nodes();
                    node = OSM2WKT.getNodebyID(str[1], NodeWikiPath);
                    wiki.setWKT(OSM2WKT.node2WKT(node));
                }

                if(str[0].equals("way")) {
                    Way way = new Way();
                    way = OSM2WKT.getWaybyID(str[1], WayPath);
                    wiki.setWKT(OSM2WKT.way2WKT(way, NodePath));
                }

                if(str[0].equals("relation")) {
                    Relations relation = new Relations();
                    relation = OSM2WKT.getRelationByID(str[1], RelationPath);
                    wiki.setWKT(OSM2WKT.relation2WKT(relation, NodePath, WayPath, RelationPath));
                }

                wiki.setURI("http://www.wikidata.org/wiki/" + wiki.getID());

                // create the resource and add the properties cascading style
                String wkt = wiki.getWKT();
                if(wiki.getWKT() != null) {
                    if(!wiki.getName_en().equals("No English Name")) {
                        String stype = osm.values().toString();
                        String stype1 = new String(stype.substring(2, stype.indexOf(",")));
                        String stype2 = new String(stype.substring(stype.indexOf(",")+2, stype.length()-2));
                        rdf_wiki = model.createResource(wiki.getURI())
                                .addProperty(OWL.sameAs,
                                        model.createResource()
                                                .addProperty(VCARD.CLASS, stype2)
                                                .addProperty(RDFS.subClassOf, stype1)
                                                .addProperty(VCARD.UID, String.valueOf(osm.keySet().toArray()[0]))
                                )
                                .addProperty(VCARD.NAME,
                                        model.createResource()
                                                .addProperty(RDFS.label, model.createLiteral(wiki.getName_en(), "en"))
                                                .addProperty(RDFS.label, model.createLiteral(wiki.getName_zh(), "zh"))
                                )
                                .addProperty(VCARD.GEO, wkt)
                                .addProperty(RDF.type, wiki.getType().get(0))
                                .addProperty(VCARD.UID, wiki.getIDType() + "/" + wiki.getID())
                        ;
                    }
                    else {
                        String stype = osm.values().toString();
                        String stype1 = new String(stype.substring(2, stype.indexOf(",")));
                        String stype2 = new String(stype.substring(stype.indexOf(",")+2, stype.length()-2));
                        rdf_wiki = model.createResource(wiki.getURI())
                                .addProperty(OWL.sameAs,
                                        model.createResource()
                                                .addProperty(VCARD.CLASS, stype2)
                                                .addProperty(RDFS.subClassOf, stype1)
                                                .addProperty(VCARD.UID, String.valueOf(osm.keySet().toArray()[0]))
                                )
                                .addProperty(VCARD.NAME, wiki.getName_zh())
                                .addProperty(VCARD.GEO, wkt)
                                .addProperty(RDF.type, wiki.getType().get(0))
                                .addProperty(VCARD.UID, wiki.getIDType() + "/" + wiki.getID())
                        ;
                    }
                }

                // now write the model in XML form to a file
                model.write(System.out, "RDF/XML-ABBREV");
                System.out.println(s);
                FileWriter out = new FileWriter(RDFfile);
                model.write( out, "RDF/XML-ABBREV" );
                /*ByteArrayOutputStream sout = new ByteArrayOutputStream();
                model.write(sout, "RDF/XML-ABBREV");
                HandleFiles.WriteFile(RDFfile, sout.toString());*/
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
        //-Addition

        String Wiki_NameEn = "F:\\Wiki-Name_EN&&ID.csv";
        String OSMwithWiki_Taiwan = "F:\\OSMwithWiki_Taiwan.csv";
        String OSMwithWiki_China = "F:\\OSMwithWiki_China.csv";
        String RDFfile_Taiwan = "F:\\RDF_Wiki_Taiwan.ttl";
        String RDFfile_China = "F:\\RDF_Wiki_China.ttl";
        String NodePath_Taiwan = "F:\\NodePath_Taiwan.txt";
        String NodeWikiPath_Taiwan = "F:\\NodePath(Wiki)_Taiwan.txt";
        String WayPath_Taiwan = "F:\\WayPath_Taiwan.txt";
        String RelationPath_Taiwan = "F:\\RelationPath_Taiwan.txt";
        String NodePath_China = "F:\\NodePath_China.txt";
        String NodeWikiPath_China = "F:\\NodePath(Wiki)_China.txt";
        String WayPath_China = "F:\\WayPath_China.txt";
        String RelationPath_China = "F:\\RelationPath_China.txt";


        RDF_Wiki(OSMwithWiki_Taiwan, Wiki_NameEn, RDFfile_Taiwan, NodePath_Taiwan, WayPath_Taiwan, RelationPath_Taiwan);
        RDF_Wiki(OSMwithWiki_China, Wiki_NameEn, RDFfile_China, NodePath_China, WayPath_China, RelationPath_China);
        //RDFWriter_Wiki(OSMwithWiki_Taiwan, Wiki_NameEn, RDFfile_Taiwan, NodeWikiPath_Taiwan, NodePath_Taiwan, WayPath_Taiwan, RelationPath_Taiwan);
        //RDFWriter_Wiki(OSMwithWiki_China, Wiki_NameEn, RDFfile_China, NodeWikiPath_China, NodePath_China, WayPath_China, RelationPath_China);
//
//        String Wiki_NameEn = "F:\\Wiki-Name_EN&&ID.csv";
//        String OSMwithWiki_China = "F:\\Test\\OSMwithWiki_China_30.csv";
//        String RDFfile_China = "F:\\Test\\RDF_Wiki_China_30.ttl";
//        String NodeWikiPath_China = "F:\\Test\\NodePath(Wiki)_China2_30.txt";
//        String NodePath_China = "F:\\NodePath_China.txt";
//        String WayPath_China = "F:\\WayPath_China.txt";
//        String RelationPath_China = "F:\\RelationPath_China.txt";
//        RDFWriter_Wiki(OSMwithWiki_China, Wiki_NameEn, RDFfile_China, NodeWikiPath_China, NodePath_China, WayPath_China, RelationPath_China);
//
    }

}
