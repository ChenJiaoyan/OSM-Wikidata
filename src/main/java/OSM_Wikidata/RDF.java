package OSM_Wikidata;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.VCARD;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import static OSM_Wikidata.OSM2WKT.polygonOrPolyline;
import static OSM_Wikidata.OSM_Wikidata.toUpperCaseFirstOne;

/**
 * Created by SmallApple on 2017/7/19.
 */

public class RDF extends DefaultHandler {
    private final static String XML_TAG_OSM = "osm";
    private final static String XML_TAG_NODE = "node";
    private final static String XML_TAG_ID = "id";
    private final static String XML_TAG_LAT = "lat";
    private final static String XML_TAG_LON = "lon";
    private final static String XML_TAG_WAY = "way";
    private final static String XML_TAG_ND = "nd";
    private final static String XML_TAG_REF = "ref";
    private final static String XML_TAG_NAME = "name"; //需要记录下value的tag key
    private final static String FILE_EXT_WKT = "wkt";
    private final static String FILE_EXT_OSM = "osm";

    private String curretntag = "";
    private String idcontents = "";
    private String versioncontents = "";
    private String uidioncontents = "";
    private String usercontents = "";
    private String loncontents = "";
    private String latcontents = "";
    private String changesetcontents = "";
    private String timestampcontents = "";
    private String visiblecontents = "";
    private String kvcontents = "";
    private String kvcontents_Zh = "";
    private String kvcontents_En = "";
    private String kvcontentsWiki = "";
    //private String pointids = "";
    private Vector<String> pointids;
    private Vector<Nodes> points;
    private Vector<String> nodeIDs;
    private Vector<String> wayIDs;
    private Vector<String> relationIDs;
    private Nodes nodes;
    private Way way;
    private Relations relation;

    private List<Nodes> nodeslist;
    private List<Way> waylist;
    private List<Relations> relationlist;

    //这些值作为记录name的索引
    private Integer plag = 0;
    private Integer plag_Zh = 0;
    private Integer plag_En = 0;
    //这tagN用于记录含有同一tag的节点数目，每次用完均要清零
    private Integer tagN = 0;
    //tagIF作为记录含有同一tag key&&value的判断参数
    private Integer tagIF = 0;


    //运行中国台湾的数据时
    String RDF_OSM_file = "F:\\RDF_OSM_Taiwan1";
    String RDF_Wiki_file = "F:\\RDF_Wiki_Taiwan1";
    String NodePath = "F:\\NodePath_Taiwan.txt";
    String WayPath = "F:\\WayPath_Taiwan.txt";
    String RelationPath = "F:\\RelationPath_Taiwan.txt";

    /*
    //运行中国的数据时
    String RDF_OSM_file = "F:\\RDF_OSM_China1";
    String RDF_Wiki_file = "F:\\RDF_Wiki_China1";
    String NodePath = "F:\\NodePath_China.txt";
    String WayPath = "F:\\WayPath_China.txt";
    String RelationPath = "F:\\RelationPath_China.txt";
    */
    // create an empty model
    private Model model_OSM = ModelFactory.createDefaultModel();
    // create an empty model
    private Model model_Wiki = ModelFactory.createDefaultModel();

    @Override
    public void startDocument() throws SAXException {
        nodeslist = new ArrayList<Nodes>();
        waylist = new ArrayList<Way>();
        relationlist = new ArrayList<Relations>();
        System.out.println("正在读取XML(OSM)文档，如果数据量过大需要一段时间，请耐心等待……");
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        // 对node进行操作
        if ("node".equals(qName)) { //记录下node的id和经纬度
            //先对前一节点进行处理
            /**
             * 之所以要进行这步工作，是因为可能多个node共有相同的tag，
             * 但是只有OSM文件中距离tag最近的node可以记录下tag的value&&key
             * 因此我们想办法将共有相同tag的node先存进nodelist，在endElement进行之后对他们进行统一setTag操作
             */
            if(tagIF == 1) {
                for(int i = 0; i < tagN; i++) {
                    Nodes n = new Nodes();
                    n = nodeslist.get(i);
                    n.setTag(kvcontentsWiki);
                    System.out.println("Node Id: " + n.getId() + "\tName: " + kvcontents + "Zh " + kvcontents_Zh + "En " + kvcontents_En);
                }
                RDFNode_OSM(model_OSM, nodeslist, kvcontents, kvcontents_Zh, kvcontents_En, RDF_OSM_file + "_N.ttl");
                RDFNode_Wiki(model_Wiki, nodeslist, kvcontents, kvcontents_Zh, kvcontents_En, RDF_Wiki_file + "_N.ttl");
                nodeslist.clear();;
                kvcontents = "";
                kvcontents_En = "";
                kvcontents_Zh = "";
                kvcontentsWiki = "";
                curretntag = "";
                plag = 0;
                plag_En = 0;
                plag_Zh = 0;
                tagN = 0;
                nodes = null;
                tagIF = 0;
            }
            if (attributes.getValue("id") != null && attributes.getValue("id") != "")
                idcontents = attributes.getValue("id");
            else
                idcontents = "0";
            if (attributes.getValue("lon") != null && attributes.getValue("lon") != "")
                loncontents = attributes.getValue("lon");
            else
                loncontents = "0";
            if (attributes.getValue("lat") != null && attributes.getValue("lat") != "")
                latcontents = attributes.getValue("lat");
            else
                latcontents = "0";
            curretntag = "node";
            tagN++;
            nodes = new Nodes();
            nodes.setId(idcontents);
            nodes.setLon(loncontents);
            nodes.setLat(latcontents);
            nodes.setTag(kvcontentsWiki);
            nodeslist.add(nodes);
        }

        if ("node".equals(curretntag) && "tag".equals(qName)) {
            tagIF = 1;
            String kcontents = attributes.getValue("k");
            String vcontents = attributes.getValue("v");
            if(plag == 0 || plag_Zh == 0 || plag_En == 0) {
                kvcontents = kcontents + "-" + vcontents; //这是在没有name的情况下，记录下一个key及其value作为tag
                if(kcontents.equals(XML_TAG_NAME)) { //提取出OSM实体node的name
                    plag = 1;
                    //kvcontentsN = kcontents + "=" + vcontents;
                    kvcontents = vcontents;
                }
                if(kcontents.equals(XML_TAG_NAME + ":zh")) { //提取出OSM实体node的中文名
                    plag_Zh = 1;
                    //kvcontentsN = kcontents + "=" + vcontents;
                    kvcontents_Zh = vcontents;
                }
                if(kcontents.equals(XML_TAG_NAME + ":en")) {  //提取出OSM实体node的英文名
                    plag_En = 1;
                    //kvcontentsN = kcontents + "=" + vcontents;
                    kvcontents_En = vcontents;
                }
            } else if(kcontents.equals("wikidata")) {
                kvcontentsWiki = vcontents;
            }
        }

        //对way操作
        if ("way".equals(qName)) {
            //先对前一节点进行处理
            /**
             * 之所以要进行这步工作，是因为可能多个node共有相同的tag，
             * 但是只有OSM文件中距离tag最近的node可以记录下tag的value&&key
             * 因此我们想办法将共有相同tag的node先存进nodelist，对他们进行统一setTag操作
             * 在"way".equals(qName)的情况下，这一步只会进行一次，就是记录下OSM文件中最后一个node的信息
             * OSM文件的feature记录顺序是node--->way--->relation
             */
            if(tagIF == 1) {
                for(int i = 0; i < tagN; i++) {
                    Nodes n = new Nodes();
                    n = nodeslist.get(i);
                    System.out.println("Node Id: " + n.getId() + "\tName: " + kvcontents + "Zh " + kvcontents_Zh + "En " + kvcontents_En);
                    if (attributes.getValue("id") != null && attributes.getValue("id") != "")
                        idcontents = attributes.getValue("id");
                    else
                        idcontents = "0";
                    if (attributes.getValue("lon") != null && attributes.getValue("lon") != "")
                        loncontents = attributes.getValue("lon");
                    else
                        loncontents = "0";
                    if (attributes.getValue("lat") != null && attributes.getValue("lat") != "")
                        latcontents = attributes.getValue("lat");
                    else
                        latcontents = "0";
                    n.setId(idcontents);
                    n.setLon(loncontents);
                    n.setLat(latcontents);
                    n.setTag(kvcontentsWiki);
                    RDFNode_OSM(model_OSM, nodeslist, kvcontents, kvcontents_Zh, kvcontents_En, RDF_OSM_file + "_N.ttl");
                    RDFNode_Wiki(model_Wiki, nodeslist, kvcontents, kvcontents_Zh, kvcontents_En, RDF_Wiki_file + "_N.ttl");
                }
                nodeslist.clear();;
                kvcontents = "";
                kvcontents_En = "";
                kvcontents_Zh = "";
                kvcontentsWiki = "";
                curretntag = "";
                plag = 0;
                plag_En = 0;
                plag_Zh = 0;
                tagN = 0;
                nodes = null;
                tagIF = 0;
            }
            //再对way进行操作
            System.out.println("Nodes are done!");
            way = new Way();
            pointids = new Vector<String>();
            points = new Vector<Nodes>();
            if (attributes.getValue("id") != null && attributes.getValue("id") != "")
                idcontents = attributes.getValue("id");
            else
                idcontents = "0";
            curretntag = "way";
        }
        if ("way".equals(curretntag) && "tag".equals(qName)) {
            tagIF = 1;
            String kcontents = attributes.getValue("k");
            String vcontents = attributes.getValue("v");
            if(plag == 0 || plag_Zh == 0 || plag_En == 0) {
                kvcontents = kcontents + "-" + vcontents; //这是在没有name的情况下，记录下一个key及其value作为tag
                if(kcontents.equals(XML_TAG_NAME)) { //提取出OSM实体way的name
                    plag = 1;
                    kvcontents = vcontents;
                }
                if(kcontents.equals(XML_TAG_NAME + ":zh")) { //提取出OSM实体way的中文名
                    plag_Zh = 1;
                    kvcontents_Zh = vcontents;
                }
                if(kcontents.equals(XML_TAG_NAME + ":en")) {  //提取出OSM实体way的英文名
                    plag_En = 1;
                    kvcontents_En = vcontents;
                }
            } else if(kcontents.equals("wikidata")) {
                kvcontentsWiki = vcontents;
            }
        }

        if ("way".equals(curretntag) && "nd".equals(qName)) { //对way的引用node操作
            String ref = attributes.getValue("ref");
            pointids.add(ref);
        }

        //对relation操作
        if ("relation".equals(qName)) {
            System.out.println("Ways are done!");
            nodeIDs = new Vector<String>();
            wayIDs = new Vector<String>();
            relationIDs = new Vector<String>();
            if (attributes.getValue("id") != null && attributes.getValue("id") != "")
                idcontents = attributes.getValue("id");
            else
                idcontents = "0";
            curretntag = "relation";
        }
        if ("relation".equals(curretntag) && "tag".equals(qName)) {
            tagIF = 1;
            String kcontents = attributes.getValue("k");
            String vcontents = attributes.getValue("v");
            if(plag == 0 || plag_Zh == 0 || plag_En == 0) {
                kvcontents = kcontents + "-" + vcontents; //这是在没有name的情况下，记录下一个key及其value作为tag
                if(kcontents.equals(XML_TAG_NAME)) { //提取出OSM实体relation的name
                    plag = 1;
                    kvcontents = vcontents;
                }
                if(kcontents.equals(XML_TAG_NAME + ":zh")) { //提取出OSM实体relation的中文名
                    plag_Zh = 1;
                    kvcontents_Zh = vcontents;
                }
                if(kcontents.equals(XML_TAG_NAME + ":en")) {  //提取出OSM实体relation的英文名
                    plag_En = 1;
                    kvcontents_En = vcontents;
                }
            } else if(kcontents.equals("wikidata")) {
                kvcontentsWiki = vcontents;
            }
        }

        if ("relation".equals(curretntag) && "member".equals(qName)) { //对relation引用的node和way操作
            String member = attributes.getValue("type");
            if(member.equals("node")) {
                nodeIDs.add(attributes.getValue("ref"));
            }
            if(member.equals("way")) {
                wayIDs.add(attributes.getValue("ref"));
            }
            if(member.equals("relation")) {
                relationIDs.add(attributes.getValue("ref"));
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        //对node进行处理

        //对way进行处理
        if ("way".equals(qName)) {
            way.setId(idcontents);
            way.setTag(kvcontentsWiki);
            way.setPointids(pointids);
            System.out.println("Way Id:" + way.getId() + "\tName: " + kvcontents + "Zh " + kvcontents_Zh + "En " + kvcontents_En);
            if (polygonOrPolyline(way.getPointids())) {
                System.out.print("Polygon");
            } else {
                System.out.print("Polyline");
            }
            System.out.println(way.getPointids());
            RDFWay_OSM(model_OSM, way, kvcontents, kvcontents_Zh, kvcontents_En, RDF_OSM_file + "_W.ttl", NodePath);
            RDFWay_Wiki(model_Wiki, way, kvcontents, kvcontents_Zh, kvcontents_En, RDF_OSM_file + "_W.ttl", NodePath);
            kvcontents = "";
            curretntag = "";
            pointids = null;
            kvcontents_En = "";
            kvcontents_Zh = "";
            kvcontentsWiki = "";
            plag = 0;
            plag_En = 0;
            plag_Zh = 0;
            way = null;
        }

        //对relation进行处理
        if ("relation".equals(qName)) {
            relation = new Relations();
            relation.setId(idcontents);
            relation.setTag(kvcontentsWiki);
            relation.setnodeIDs(nodeIDs);
            relation.setwayIDs(wayIDs);
            relation.setrelationIDs(relationIDs);
            System.out.println("Relation Id:" + relation.getId() + "\tName: " + kvcontents + "Zh " + kvcontents_Zh + "En " + kvcontents_En);
            System.out.println(relation.getnodeIDs() + ", " + relation.getwayIDs() + ", " + relation.getrelationIDs());
            RDFRelation_OSM(model_OSM, relation, kvcontents, kvcontents_Zh, kvcontents_En, RDF_OSM_file + "_R.ttl", NodePath, WayPath, RelationPath);
            RDFRelation_Wiki(model_Wiki, relation, kvcontents, kvcontents_Zh, kvcontents_En, RDF_OSM_file + "_R.ttl", NodePath, WayPath, RelationPath);
            kvcontents = "";
            kvcontents_En = "";
            kvcontents_Zh = "";
            kvcontentsWiki = "";
            curretntag = "";
            nodeIDs = null;
            wayIDs = null;
            relationIDs = null;
            plag = 0;
            plag_En = 0;
            plag_Zh = 0;
            relation = null;
        }
    }

    @Override
    public void endDocument() throws SAXException {
        //对前面没有处理完的waylist进行处理
        if (!waylist.isEmpty()) {
            for (int i = 0; i < waylist.size(); i++) {
                //streets.put(waylist.get(i).getId(), waylist.get(i).getPointids());
                System.out.println("Way Id:" + waylist.get(i).getId() + "\tName: " + waylist.get(i).getTag());
                try {
                    if (polygonOrPolyline(waylist.get(i).getPointids()))
                        System.out.println("Polygon" + waylist.get(i).getPointids()); //polygon;
                    else
                        System.out.println("Polyline" + waylist.get(i).getPointids());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            waylist.clear();
        }
        //对前面没有处理完的relationlist进行处理
        if (!relationlist.isEmpty()) {
            for (int i = 0; i < relationlist.size(); i++) {
                System.out.println("Relation Id:" + relationlist.get(i).getId() + "\tName: " + relationlist.get(i).getTag());
                System.out.println(relationlist.get(i).getnodeIDs() + ", " + relationlist.get(i).getwayIDs() + ", " + relationlist.get(i).getrelationIDs());
            }
            relationlist.clear();
        }
    }
    public static void RDFNode_OSM(Model model, List<Nodes> OSMlist, String Name, String Name_zh, String Name_en, String RDFfile) {
        String entity = "node";
        OSM osm = new OSM();
        Nodes node = new Nodes();
        Resource rdf_osm;
        for(int i = 0; i < OSMlist.size(); i++) {
            node = OSMlist.get(i);
            osm.setWKT(OSM2WKT.node2WKT(node));
            osm.setIDType("osm");
            ArrayList type = new ArrayList();
            type.add("OSMEntity");
            type.add("OSM" + toUpperCaseFirstOne(entity));
            osm.setType(type);
            osm.setID(node.getId());
            osm.setOSMType(entity);
            // 对name的处理
            if(Name_zh == "" || Name_en == "") {
                osm.setName_zh(Name);
            } else {
                osm.setName_zh(Name_zh);
                osm.setName_en(Name_en);
            }
            HashMap<String, ArrayList<String>> wiki = new HashMap<String, ArrayList<String>>();
            ArrayList t = new ArrayList();
            t.add("WikidataEntity");
            wiki.put("/wikidata/" + node.getTag(), t);
            osm.setSameAs(wiki);
            osm.setURI("http://openstreetmap.org/" + osm.getOSMType() + "/" + osm.getID());
            String stype = String.valueOf(wiki.values().toArray()[0]);
            String st = new String(stype.substring(1, stype.length()-1));
            // and add the properties cascading style
            String wkt = osm.getWKT();
            try {
                File rdfFile = new File(RDFfile);
                if (rdfFile.exists()) rdfFile.delete();
                rdfFile.createNewFile();
                if(osm.getWKT() != null) {
                    if (osm.getName_en() != "") {
                        rdf_osm = model.createResource(osm.getURI())
                                .addProperty(OWL.sameAs,
                                        model.createResource()
                                                .addProperty(org.apache.jena.vocabulary.RDF.type, st)
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
                                .addProperty(org.apache.jena.vocabulary.RDF.type, osm.OSM_Type)
                                .addProperty(VCARD.UID, osm.getIDType() + "/" + osm.getID())
                        ;
                    } else {
                        rdf_osm = model.createResource(osm.getURI())
                                .addProperty(OWL.sameAs,
                                        model.createResource()
                                                .addProperty(org.apache.jena.vocabulary.RDF.type, st)
                                                .addProperty(VCARD.UID, String.valueOf(wiki.keySet().toArray()[0]))
                                )
                                .addProperty(VCARD.NAME, osm.getName_zh())
                                .addProperty(VCARD.GEO, wkt)
                                .addProperty(VCARD.CLASS, osm.getType().get(1))
                                .addProperty(RDFS.subClassOf, osm.getType().get(0))
                                .addProperty(org.apache.jena.vocabulary.RDF.type, osm.OSM_Type)
                                .addProperty(VCARD.UID, osm.getIDType() + "/" + osm.getID())
                        ;
                    }
                }
                model.write(System.out, "RDF/XML-ABBREV");
                FileWriter out = new FileWriter(RDFfile);
                model.write( out, "RDF/XML-ABBREV" );
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
    public static void RDFWay_OSM(Model model, Way way, String Name, String Name_zh, String Name_en, String RDFfile, String nodePath) {
        String entity = "node";
        OSM osm = new OSM();
        osm.setWKT(OSM2WKT.way2WKT(way, nodePath));
        osm.setIDType("osm");
        ArrayList type = new ArrayList();
        type.add("OSMEntity");
        type.add("OSM" + toUpperCaseFirstOne(entity));
        osm.setType(type);
        osm.setID(way.getId());
        osm.setOSMType(entity);
        // 对name的处理
        if(Name_zh == "" || Name_en == "") {
            osm.setName_zh(Name);
        } else {
            osm.setName_zh(Name_zh);
            osm.setName_en(Name_en);
        }
        HashMap<String, ArrayList<String>> wiki = new HashMap<String, ArrayList<String>>();
        ArrayList t = new ArrayList();
        t.add("WikidataEntity");
        wiki.put("/wikidata/" + way.getTag(), t);
        osm.setSameAs(wiki);
        osm.setURI("http://openstreetmap.org/" + osm.getOSMType() + "/" + osm.getID());
        String stype = String.valueOf(wiki.values().toArray()[0]);
        String st = new String(stype.substring(1, stype.length()-1));
        // and add the properties cascading style
        String wkt = osm.getWKT();
        // create the resource
        Resource rdf_osm;
        try {
            File rdfFile = new File(RDFfile);
            if (rdfFile.exists()) rdfFile.delete();
            rdfFile.createNewFile();
            if(osm.getWKT() != null) {
                if (osm.getName_en() != "") {
                    rdf_osm = model.createResource(osm.getURI())
                            .addProperty(OWL.sameAs,
                                    model.createResource()
                                            .addProperty(org.apache.jena.vocabulary.RDF.type, st)
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
                            .addProperty(org.apache.jena.vocabulary.RDF.type, osm.OSM_Type)
                            .addProperty(VCARD.UID, osm.getIDType() + "/" + osm.getID())
                    ;
                } else {
                    rdf_osm = model.createResource(osm.getURI())
                            .addProperty(OWL.sameAs,
                                    model.createResource()
                                            .addProperty(org.apache.jena.vocabulary.RDF.type, st)
                                            .addProperty(VCARD.UID, String.valueOf(wiki.keySet().toArray()[0]))
                            )
                            .addProperty(VCARD.NAME, osm.getName_zh())
                            .addProperty(VCARD.GEO, wkt)
                            .addProperty(VCARD.CLASS, osm.getType().get(1))
                            .addProperty(RDFS.subClassOf, osm.getType().get(0))
                            .addProperty(org.apache.jena.vocabulary.RDF.type, osm.OSM_Type)
                            .addProperty(VCARD.UID, osm.getIDType() + "/" + osm.getID())
                    ;
                }
            }
            model.write(System.out, "RDF/XML-ABBREV");
            FileWriter out = new FileWriter(RDFfile);
            model.write( out, "RDF/XML-ABBREV" );
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
    public static void RDFRelation_OSM(Model model, Relations relation, String Name, String Name_zh, String Name_en, String RDFfile, String nodePath, String wayPath, String relationPath) {
        String entity = "relation";
        OSM osm = new OSM();
        osm.setWKT(OSM2WKT.relation2WKT(relation, nodePath, wayPath, relationPath));
        osm.setIDType("osm");
        ArrayList type = new ArrayList();
        type.add("OSMEntity");
        type.add("OSM" + toUpperCaseFirstOne(entity));
        osm.setType(type);
        osm.setID(relation.getId());
        osm.setOSMType(entity);
        // 对name的处理
        if(Name_zh == "" || Name_en == "") {
            osm.setName_zh(Name);
        } else {
            osm.setName_zh(Name_zh);
            osm.setName_en(Name_en);
        }
        HashMap<String, ArrayList<String>> wiki = new HashMap<String, ArrayList<String>>();
        ArrayList t = new ArrayList();
        t.add("WikidataEntity");
        wiki.put("/wikidata/" + relation.getTag(), t);
        osm.setSameAs(wiki);
        osm.setURI("http://openstreetmap.org/" + osm.getOSMType() + "/" + osm.getID());
        String stype = String.valueOf(wiki.values().toArray()[0]);
        String st = new String(stype.substring(1, stype.length()-1));
        // and add the properties cascading style
        String wkt = osm.getWKT();
        // create the resource
        Resource rdf_osm;
        try {
            File rdfFile = new File(RDFfile);
            if (rdfFile.exists()) rdfFile.delete();
            rdfFile.createNewFile();
            if(osm.getWKT() != null) {
                if (osm.getName_en() != "") {
                    rdf_osm = model.createResource(osm.getURI())
                            .addProperty(OWL.sameAs,
                                    model.createResource()
                                            .addProperty(org.apache.jena.vocabulary.RDF.type, st)
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
                            .addProperty(org.apache.jena.vocabulary.RDF.type, osm.OSM_Type)
                            .addProperty(VCARD.UID, osm.getIDType() + "/" + osm.getID())
                    ;
                } else {
                    rdf_osm = model.createResource(osm.getURI())
                            .addProperty(OWL.sameAs,
                                    model.createResource()
                                            .addProperty(org.apache.jena.vocabulary.RDF.type, st)
                                            .addProperty(VCARD.UID, String.valueOf(wiki.keySet().toArray()[0]))
                            )
                            .addProperty(VCARD.NAME, osm.getName_zh())
                            .addProperty(VCARD.GEO, wkt)
                            .addProperty(VCARD.CLASS, osm.getType().get(1))
                            .addProperty(RDFS.subClassOf, osm.getType().get(0))
                            .addProperty(org.apache.jena.vocabulary.RDF.type, osm.OSM_Type)
                            .addProperty(VCARD.UID, osm.getIDType() + "/" + osm.getID())
                    ;
                }
            }
            model.write(System.out, "RDF/XML-ABBREV");
            FileWriter out = new FileWriter(RDFfile);
            model.write( out, "RDF/XML-ABBREV" );
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
    public static void RDFNode_Wiki(Model model, List<Nodes> OSMlist, String Name, String Name_zh, String Name_en, String RDFfile) {
        String entity = "node";
        Wikidata wiki = new Wikidata();
        Nodes node = new Nodes();
        // create the resource
        Resource rdf_wiki;
        for(int i = 0; i < OSMlist.size(); i++) {
            node = OSMlist.get(i);
            wiki.setWKT(OSM2WKT.node2WKT(node));
            wiki.setIDType("wikidata");
            ArrayList type = new ArrayList();
            type.add("WikidataEntity");
            wiki.setType(type);
            wiki.setID(node.getTag());
            // 对name的处理
            if (Name_zh == "" || Name_en == "") {
                wiki.setName_zh(Name);
            } else {
                wiki.setName_zh(Name_zh);
                wiki.setName_en(Name_en);
            }
            ArrayList t = new ArrayList();
            t.add("OSMEntity");
            t.add("OSM" + toUpperCaseFirstOne(entity));
            HashMap osm = new HashMap();
            osm.put("/osm/" + entity + "/" + node.getId(), t);
            wiki.setSameAs(osm);
            wiki.setURI("http://www.wikidata.org/wiki/" + wiki.getID());
            try {
                File rdfFile = new File(RDFfile);
                if (rdfFile.exists()) rdfFile.delete();
                rdfFile.createNewFile();
                // create the resource and add the properties cascading style
                String wkt = wiki.getWKT();
                if (wiki.getWKT() != null) {
                    if (wiki.getName_en() != "") {
                        String stype = osm.values().toString();
                        String stype1 = new String(stype.substring(2, stype.indexOf(",")));
                        String stype2 = new String(stype.substring(stype.indexOf(",") + 2, stype.length() - 2));
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
                                .addProperty(org.apache.jena.vocabulary.RDF.type, wiki.getType().get(0))
                                .addProperty(VCARD.UID, wiki.getIDType() + "/" + wiki.getID())
                        ;
                    } else {
                        String stype = osm.values().toString();
                        String stype1 = new String(stype.substring(2, stype.indexOf(",")));
                        String stype2 = new String(stype.substring(stype.indexOf(",") + 2, stype.length() - 2));
                        rdf_wiki = model.createResource(wiki.getURI())
                                .addProperty(OWL.sameAs,
                                        model.createResource()
                                                .addProperty(VCARD.CLASS, stype2)
                                                .addProperty(RDFS.subClassOf, stype1)
                                                .addProperty(VCARD.UID, String.valueOf(osm.keySet().toArray()[0]))
                                )
                                .addProperty(VCARD.NAME, wiki.getName_zh())
                                .addProperty(VCARD.GEO, wkt)
                                .addProperty(org.apache.jena.vocabulary.RDF.type, wiki.getType().get(0))
                                .addProperty(VCARD.UID, wiki.getIDType() + "/" + wiki.getID())
                        ;
                    }
                }
                // now write the model in XML form to a file
                model.write(System.out, "RDF/XML-ABBREV");
                FileWriter out = new FileWriter(RDFfile);
                model.write(out, "RDF/XML-ABBREV");
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
    public static void RDFWay_Wiki(Model model, Way way, String Name, String Name_zh, String Name_en, String RDFfile, String nodePath) {
        String entity = "way";
        Wikidata wiki = new Wikidata();
        // create the resource
        Resource rdf_wiki;
        wiki.setWKT(OSM2WKT.way2WKT(way, nodePath));
        wiki.setIDType("wikidata");
        ArrayList type = new ArrayList();
        type.add("WikidataEntity");
        wiki.setType(type);
        wiki.setID(way.getTag());
        // 对name的处理
        if (Name_zh == "" || Name_en == "") {
            wiki.setName_zh(Name);
        } else {
            wiki.setName_zh(Name_zh);
            wiki.setName_en(Name_en);
        }
        ArrayList t = new ArrayList();
        t.add("OSMEntity");
        t.add("OSM" + toUpperCaseFirstOne(entity));
        HashMap osm = new HashMap();
        osm.put("/osm/" + entity + "/" + way.getId(), t);
        wiki.setSameAs(osm);
        wiki.setURI("http://www.wikidata.org/wiki/" + wiki.getID());
        try {
            File rdfFile = new File(RDFfile);
            if (rdfFile.exists()) rdfFile.delete();
            rdfFile.createNewFile();
            // create the resource and add the properties cascading style
            String wkt = wiki.getWKT();
            if (wiki.getWKT() != null) {
                if (wiki.getName_en() != "") {
                    String stype = osm.values().toString();
                    String stype1 = new String(stype.substring(2, stype.indexOf(",")));
                    String stype2 = new String(stype.substring(stype.indexOf(",") + 2, stype.length() - 2));
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
                            .addProperty(org.apache.jena.vocabulary.RDF.type, wiki.getType().get(0))
                            .addProperty(VCARD.UID, wiki.getIDType() + "/" + wiki.getID())
                    ;
                } else {
                    String stype = osm.values().toString();
                    String stype1 = new String(stype.substring(2, stype.indexOf(",")));
                    String stype2 = new String(stype.substring(stype.indexOf(",") + 2, stype.length() - 2));
                    rdf_wiki = model.createResource(wiki.getURI())
                            .addProperty(OWL.sameAs,
                                    model.createResource()
                                            .addProperty(VCARD.CLASS, stype2)
                                            .addProperty(RDFS.subClassOf, stype1)
                                            .addProperty(VCARD.UID, String.valueOf(osm.keySet().toArray()[0]))
                            )
                            .addProperty(VCARD.NAME, wiki.getName_zh())
                            .addProperty(VCARD.GEO, wkt)
                            .addProperty(org.apache.jena.vocabulary.RDF.type, wiki.getType().get(0))
                            .addProperty(VCARD.UID, wiki.getIDType() + "/" + wiki.getID())
                    ;
                }
            }
            // now write the model in XML form to a file
            model.write(System.out, "RDF/XML-ABBREV");
            FileWriter out = new FileWriter(RDFfile);
            model.write(out, "RDF/XML-ABBREV");
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
    public static void RDFRelation_Wiki(Model model, Relations relation, String Name, String Name_zh, String Name_en, String RDFfile, String nodePath, String wayPath, String relationPath) {
        String entity = "relation";
        Wikidata wiki = new Wikidata();
        // create the resource
        Resource rdf_wiki;
        wiki.setWKT(OSM2WKT.relation2WKT(relation, nodePath, wayPath, relationPath));
        wiki.setIDType("wikidata");
        ArrayList type = new ArrayList();
        type.add("WikidataEntity");
        wiki.setType(type);
        wiki.setID(relation.getTag());
        // 对name的处理
        if (Name_zh == "" || Name_en == "") {
            wiki.setName_zh(Name);
        } else {
            wiki.setName_zh(Name_zh);
            wiki.setName_en(Name_en);
        }
        ArrayList t = new ArrayList();
        t.add("OSMEntity");
        t.add("OSM" + toUpperCaseFirstOne(entity));
        HashMap osm = new HashMap();
        osm.put("/osm/" + entity + "/" + relation.getId(), t);
        wiki.setSameAs(osm);
        wiki.setURI("http://www.wikidata.org/wiki/" + wiki.getID());
        try {
            File rdfFile = new File(RDFfile);
            if (rdfFile.exists()) rdfFile.delete();
            rdfFile.createNewFile();
            // create the resource and add the properties cascading style
            String wkt = wiki.getWKT();
            if (wiki.getWKT() != null) {
                if (wiki.getName_en() != "") {
                    String stype = osm.values().toString();
                    String stype1 = new String(stype.substring(2, stype.indexOf(",")));
                    String stype2 = new String(stype.substring(stype.indexOf(",") + 2, stype.length() - 2));
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
                            .addProperty(org.apache.jena.vocabulary.RDF.type, wiki.getType().get(0))
                            .addProperty(VCARD.UID, wiki.getIDType() + "/" + wiki.getID())
                    ;
                } else {
                    String stype = osm.values().toString();
                    String stype1 = new String(stype.substring(2, stype.indexOf(",")));
                    String stype2 = new String(stype.substring(stype.indexOf(",") + 2, stype.length() - 2));
                    rdf_wiki = model.createResource(wiki.getURI())
                            .addProperty(OWL.sameAs,
                                    model.createResource()
                                            .addProperty(VCARD.CLASS, stype2)
                                            .addProperty(RDFS.subClassOf, stype1)
                                            .addProperty(VCARD.UID, String.valueOf(osm.keySet().toArray()[0]))
                            )
                            .addProperty(VCARD.NAME, wiki.getName_zh())
                            .addProperty(VCARD.GEO, wkt)
                            .addProperty(org.apache.jena.vocabulary.RDF.type, wiki.getType().get(0))
                            .addProperty(VCARD.UID, wiki.getIDType() + "/" + wiki.getID())
                    ;
                }
            }
            // now write the model in XML form to a file
            model.write(System.out, "RDF/XML-ABBREV");
            FileWriter out = new FileWriter(RDFfile);
            model.write(out, "RDF/XML-ABBREV");
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public boolean readOSM(String filePath) {
        System.out.println("reading in openstreetmap xml ...");
        try {
            // check if file exists
            File file = new File(filePath);
            if (!file.exists()) {
                System.out.println("osm file " + filePath + " does not exist");
                return false;
            }
            // read in xml
            InputStream inStream = null;
            try {
                inStream = new FileInputStream(filePath);
                SAXParserFactory saxfac = SAXParserFactory.newInstance();
                SAXParser saxParser = saxfac.newSAXParser();
                saxParser.parse(inStream, this);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    inStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.out.println("reading osm file failed: " + e.getLocalizedMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void readRDF (String inputFileName) {
        // create an empty model
        Model model = ModelFactory.createDefaultModel();

        // use the FileManager to find the input file
        InputStream in = FileManager.get().open(inputFileName);
        if (in == null) {
            throw new IllegalArgumentException(
                    "File: " + inputFileName + " not found");
        }

        // read the RDF/XML file
        model.read(in, null);

        // write it to standard out
        model.write(System.out);
    }

    public static void main(String args[]) {
        String OSMFilePath_Taiwan = "F:\\OSMwithWiki_Taiwan.osm";
        String OSMFilePath_China = "F:\\OSMwithWiki_China.osm";
        RDF rdf = new RDF();
//        rdf.readOSM(OSMFilePath_China);
//        rdf.readOSM(OSMFilePath_Taiwan);
//        readRDF("F:\\RDF_Wiki_Taiwan.ttl");
    }
}
