package OSM_Wikidata;

import FileHandle.HandleFiles;
import oracle.spatial.geometry.JGeometry;
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
import org.slf4j.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import oracle.spatial.util.*;

import static OSM_Wikidata.OSM2WKT.polygonOrPolyline;
import static OSM_Wikidata.OSM_Wikidata.toUpperCaseFirstOne;

import OSM.*;
/**
 * Created by SmallApple on 2017/7/19.
 */

public class RDF extends DefaultHandler {

    private final static String XML_TAG_Index = "wikidata"; //需要找到存在wikidata tag key的实体，并记录下这个tag的value
    private final static String XML_TAG_NAME = "name"; //需要记录下value的tag key
    private final static String XML_TAG_OSM = "osm";
    private final static String XML_TAG_NODE = "node";
    private final static String XML_TAG_ID = "id";
    private final static String XML_TAG_LAT = "lat";
    private final static String XML_TAG_LON = "lon";
    private final static String XML_TAG_WAY = "way";
    private final static String XML_TAG_ND = "nd";
    private final static String XML_TAG_REF = "ref";
    private final static String XML_TAG_Relation = "relation";
    private final static String XML_TAG_MEMBER = "member";
    private final static String XML_TAG_TYPE = "type";
    private final static String FILE_EXT_WKT = "wkt";
    private final static String FILE_EXT_OSM = "osm";

    private String idcontents = "";
    private String loncontents = "";
    private String latcontents = "";
    /*
    private String versioncontents = "";
    private String uidioncontents = "";
    private String usercontents = "";
    private String changesetcontents = "";
    private String timestampcontents = "";
    private String visiblecontents = "";
    */
    private String curretntag = "";
    private String kvcontents = "";
    private String kvcontents_Zh = "";
    private String kvcontents_En = "";
    private String kvcontentsWiki = "";
    private Vector<String> pointids;
    private Vector<Nodes> points;
    private Vector<String> nodeIDs;
    private Vector<String> wayIDs;
    private Vector<String> relationIDs;
    private List<Nodes> nodeslist;
    private Way way;
    private Relation relation;

    //这些值作为记录name的索引
    private Integer plag = 0;
    private Integer plag_Zh = 0;
    private Integer plag_En = 0;
    //这tagN用于记录含有同一tag的节点数目，每次用完均要清零
    private Integer tagN = 0;
    //saveNode、saveWay作为包含node、way的模型是否已存入文件的判断参数
    private Integer saveNode = 0;
    private Integer saveWay = 0;

    private String rootPath = "F:/SmallApple/OSM-Wikidata_data/other/";
    private String rootRDF = rootPath + "RDF_OSM_Australia";
    //ForServer
    //private String rootPath = "/home/dsm/OSM-Wikidata/Result_the end/";

/*
    //运行中国台湾的数据时
    String RDF_OSM_file = rootPath + "RDF_OSM_Taiwan";
    String RDF_Wiki_file = rootPath + "1055534595RDF_Wiki_Taiwan";
    String NodePath = rootPath + "NodePath_Taiwan.txt";
    String WayPath = rootPath + "WayPath_Taiwan.txt";
    String RelationPath = rootPath + "RelationPath_Taiwan.txt";
*/
/*
    //运行中国的数据时
    String RDF_OSM_file = rootPath + "RDF_OSM_China";
    String RDF_Wiki_file = rootPath + "RDF_Wiki_China";
    String NodePath = rootPath + "NodePath_China.txt";
    String WayPath = rootPath + "WayPath_China.txt";
    String RelationPath = rootPath + "RelationPath_China.txt";
*/
    String RDF_OSM_file = rootPath + "RDF_OSM_Australia";
    String RDF_Wiki_file = rootPath + "RDF_Wiki_Australia";
    String NodePath = rootPath + "NodePath_Australia.txt";
    String WayPath = rootPath + "WayPath_Australia.txt";
    String RelationPath = rootPath + "RelationPath_Australia.txt";
    
    // create an empty model
    private Model model_OSM = ModelFactory.createDefaultModel();
    // create an empty model
    private Model model_Wiki = ModelFactory.createDefaultModel();

    @Override
    public void startDocument() throws SAXException {
        nodeslist = new ArrayList<Nodes>();
        System.out.println("正在读取XML(OSM)文档，如果数据量过大需要一段时间，请耐心等待……");
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        /**
         * 对node进行操作
         */
        if (XML_TAG_NODE.equals(qName)) {

            if (attributes.getValue(XML_TAG_ID) != null && attributes.getValue(XML_TAG_ID) != "")
                idcontents = attributes.getValue(XML_TAG_ID);
            else
                idcontents = "0";
            if (attributes.getValue(XML_TAG_LON) != null && attributes.getValue(XML_TAG_LON) != "")
                loncontents = attributes.getValue(XML_TAG_LON);
            else
                loncontents = "0";
            if (attributes.getValue(XML_TAG_LAT) != null && attributes.getValue(XML_TAG_LAT) != "")
                latcontents = attributes.getValue(XML_TAG_LAT);
            else
                latcontents = "0";
            curretntag = XML_TAG_NODE;
            tagN++;
            Nodes n = new Nodes();
            n.setId(idcontents);
            n.setLon(loncontents);
            n.setLat(latcontents);
            nodeslist.add(n);
            n = null;
        }

        if (XML_TAG_NODE.equals(curretntag) && "tag".equals(qName)) {
            String kcontents = attributes.getValue("k");
            String vcontents = attributes.getValue("v");
            if(plag == 0) {
                kvcontents = kcontents + "-" + vcontents; //这是在没有name的情况下，记录下一个key及其value作为tag
                if(kcontents.equals(XML_TAG_NAME)) { //提取出OSM实体node的name
                    plag = 1;
                    kvcontents = vcontents;
                }
            }
            if(plag_Zh == 0 && kcontents.equals(XML_TAG_NAME + ":zh")) { //提取出OSM实体node的中文名
                plag_Zh = 1;
                kvcontents_Zh = vcontents;
            }
            if(plag_En == 0 && kcontents.equals(XML_TAG_NAME + ":en")) {  //提取出OSM实体node的英文名
                plag_En = 1;
                kvcontents_En = vcontents;
            }
            if(kcontents.equals(XML_TAG_Index)) {
                kvcontentsWiki = vcontents;
            }
        }
        /**
         * 对way操作
         */
        if (XML_TAG_WAY.equals(qName)) {
            way = new Way();
            pointids = new Vector<String>();
            points = new Vector<Nodes>();
            if (attributes.getValue(XML_TAG_ID) != null && attributes.getValue(XML_TAG_ID) != "")
                idcontents = attributes.getValue(XML_TAG_ID);
            else
                idcontents = "0";
            curretntag = XML_TAG_WAY;
        }
        if (XML_TAG_WAY.equals(curretntag) && "tag".equals(qName)) {
            String kcontents = attributes.getValue("k");
            String vcontents = attributes.getValue("v");
            if(plag == 0) {
                kvcontents = kcontents + "-" + vcontents; //这是在没有name的情况下，记录下一个key及其value作为name tag
                if(kcontents.equals(XML_TAG_NAME)) { //提取出OSM实体way的name
                    plag = 1;
                    kvcontents = vcontents;
                }
            }
            if(plag_Zh == 0 && kcontents.equals(XML_TAG_NAME + ":zh")) { //提取出OSM实体way的中文名
                plag_Zh = 1;
                kvcontents_Zh = vcontents;
            }
            if(plag_En == 0 && kcontents.equals(XML_TAG_NAME + ":en")) {  //提取出OSM实体way的英文名
                plag_En = 1;
                kvcontents_En = vcontents;
            }
            if(kcontents.equals(XML_TAG_Index)) {
                kvcontentsWiki = vcontents;
            }
        }

        if (XML_TAG_WAY.equals(curretntag) && XML_TAG_ND.equals(qName)) { //对way的引用node操作
            String ref = attributes.getValue(XML_TAG_REF);
            pointids.add(ref);
        }
        /**
         * 对relation操作
         */
        if (XML_TAG_Relation.equals(qName)) {
            nodeIDs = new Vector<String>();
            wayIDs = new Vector<String>();
            relationIDs = new Vector<String>();
            if (attributes.getValue(XML_TAG_ID) != null && attributes.getValue(XML_TAG_ID) != "")
                idcontents = attributes.getValue(XML_TAG_ID);
            else
                idcontents = "0";
            curretntag = XML_TAG_Relation;
        }
        if (XML_TAG_Relation.equals(curretntag) && "tag".equals(qName)) {
            String kcontents = attributes.getValue("k");
            String vcontents = attributes.getValue("v");
            if(plag == 0) {
                kvcontents = kcontents + "-" + vcontents; //这是在没有name的情况下，记录下一个key及其value作为name tag
                if(kcontents.equals(XML_TAG_NAME)) { //提取出OSM实体node的name
                    plag = 1;
                    kvcontents = vcontents;
                }
            }
            if(plag_Zh == 0 && kcontents.equals(XML_TAG_NAME + ":zh")) { //提取出OSM实体relation的中文名
                plag_Zh = 1;
                kvcontents_Zh = vcontents;
            }
            if(plag_En == 0 && kcontents.equals(XML_TAG_NAME + ":en")) {  //提取出OSM实体relation的英文名
                plag_En = 1;
                kvcontents_En = vcontents;
            }
            if(kcontents.equals(XML_TAG_Index)) {
                kvcontentsWiki = vcontents;
            }
        }

        if (XML_TAG_Relation.equals(curretntag) && XML_TAG_MEMBER.equals(qName)) { //对relation引用的node和way操作
            String member = attributes.getValue(XML_TAG_TYPE);
            if(member.equals(XML_TAG_NODE)) {
                nodeIDs.add(attributes.getValue(XML_TAG_REF));
            }
            if(member.equals(XML_TAG_WAY)) {
                wayIDs.add(attributes.getValue(XML_TAG_REF));
            }
            if(member.equals(XML_TAG_Relation)) {
                relationIDs.add(attributes.getValue(XML_TAG_REF));
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        //对node进行处理
        if (XML_TAG_NODE.equals(qName)) {
            if(kvcontentsWiki != "") {
                /**
                 * 之所以要进行这步工作，是因为可能多个node共有相同的tag，
                 * 但是只有OSM文件中距离tag最近的node可以记录下tag的value&&key
                 * 因此我们想办法将共有相同tag的node先存进nodelist，在endElement进行之后对他们进行统一setTag操作
                 */
                for (int i = 0; i < tagN; i++) {
                    Nodes n = new Nodes();
                    n = nodeslist.get(i);
                    n.setTag(kvcontentsWiki);
                    n.setLabel(kvcontents);
                    n.setName_zh(kvcontents_Zh);
                    n.setName_en(kvcontents_En);
                    // 将存在wikidata链接的node存进model_OSM
                    model_OSM = RDF_OSM(model_OSM, node2OSM(n));
                    //model_Wiki = RDF_Wiki(model_Wiki, node2OSM(n));
                    System.out.println("Node Id: " + n.getId() + "\tName: " + kvcontents + "\tZh: " + kvcontents_Zh + "\tEn: " + kvcontents_En + "\tWiki: " + kvcontentsWiki);
                    n = null;
                }
            }

            nodeslist.clear();
            kvcontents = "";
            kvcontents_En = "";
            kvcontents_Zh = "";
            kvcontentsWiki = "";
            curretntag = "";
            plag = 0;
            plag_En = 0;
            plag_Zh = 0;
            tagN = 0;
        }

        //对way进行处理
        if (XML_TAG_WAY.equals(qName)) {
            // 存model（node）
            if(saveNode == 0) {
                writeSelectedRDF(model_OSM, rootRDF + "_N.xml", "RDF/XML-ABBREV");
                writeSelectedRDF(model_OSM, rootRDF + "_N.ttl", "Turtle");
//                writeSelectedRDF(model_Wiki, rootRDF + "_N.xml", "RDF/XML-ABBREV");
//                writeSelectedRDF(model_Wiki, rootRDF + "_N.ttl", "Turtle");
                saveNode = 1;
            }
            if(kvcontentsWiki != "") {
                way.setId(idcontents);
                way.setTag(kvcontentsWiki);
                way.setLabel(kvcontents);
                way.setName_en(kvcontents_En);
                way.setName_zh(kvcontents_Zh);
                way.setPointids(pointids);
                // 将存在wikidata链接的way存进model_OSM
                model_OSM = RDF_OSM(model_OSM, way2OSM(way, NodePath));
                //model_Wiki = RDF_Wiki(model_Wiki, way2OSM(way, NodePath));
                System.out.println("Way Id:" + way.getId() + "\tName: " + kvcontents + "\tZh: " + kvcontents_Zh + "\tEn: " + kvcontents_En + "\tWiki: " + kvcontentsWiki);
                if (polygonOrPolyline(way.getPointids())) {
                    System.out.print("Polygon");
                } else {
                    System.out.print("Polyline");
                }
                System.out.println(way.getPointids());
            }

            curretntag = "";
            kvcontents = "";
            kvcontents_En = "";
            kvcontents_Zh = "";
            kvcontentsWiki = "";
            plag = 0;
            plag_En = 0;
            plag_Zh = 0;
            way = null;
            pointids = null;
        }

        //对relation进行处理
        if (XML_TAG_Relation.equals(qName)) {
            // 存model（way）
            if(saveWay == 0) {
                writeSelectedRDF(model_OSM, rootRDF + "_W.xml", "RDF/XML-ABBREV");
                writeSelectedRDF(model_OSM, rootRDF + "_W.ttl", "Turtle");
//                writeSelectedRDF(model_Wiki, rootRDF + "_W.xml", "RDF/XML-ABBREV");
//                writeSelectedRDF(model_Wiki, rootRDF + "_W.ttl", "Turtle");
                saveWay = 1;
            }
            if(kvcontentsWiki != "") {
                relation = new Relation();
                relation.setId(idcontents);
                relation.setTag(kvcontentsWiki);
                relation.setLabel(kvcontents);
                relation.setName_en(kvcontents_En);
                relation.setName_zh(kvcontents_Zh);
                relation.setnodeIDs(nodeIDs);
                relation.setwayIDs(wayIDs);
                relation.setrelationIDs(relationIDs);
                // 将存在wikidata链接的relation存进model_OSM
                model_OSM = RDF_OSM(model_OSM, relation2OSM(relation, NodePath, WayPath, RelationPath));
                //model_Wiki = RDF_Wiki(model_Wiki, relation2OSM(relation, NodePath, WayPath, RelationPath));
                System.out.println("Relation Id:" + relation.getId() + "\tName: " + kvcontents + "\tZh: " + kvcontents_Zh + "\tEn: " + kvcontents_En + "\tWiki: " + kvcontentsWiki);
                System.out.println(relation.getnodeIDs() + ", " + relation.getwayIDs() + ", " + relation.getrelationIDs());
            }

            curretntag = "";
            kvcontents = "";
            kvcontents_En = "";
            kvcontents_Zh = "";
            kvcontentsWiki = "";
            plag = 0;
            plag_En = 0;
            plag_Zh = 0;
            nodeIDs = null;
            wayIDs = null;
            relationIDs = null;
            relation = null;
        }

    }

    @Override
    public void endDocument() throws SAXException {
        writeSelectedRDF(model_OSM, rootRDF + "_R.xml", "RDF/XML-ABBREV");
        writeSelectedRDF(model_OSM, rootRDF + "_R.ttl", "Turtle");
        //writeSelectedRDF(model_Wiki, rootRDF + "_R.xml", "RDF/XML-ABBREV");
        //writeSelectedRDF(model_Wiki, rootRDF + "_R.ttl", "Turtle");
        System.out.println("Reading OSM/XML file is done!");
    }

    public static OSM node2OSM(Nodes node) {
        OSM osm = new OSM();
        String entity = "node";
        osm.setLabel(node.getLabel());
        osm.setName_en(node.getName_en());
        osm.setName_zh(node.getName_zh());
        osm.setWKT(OSM2WKT.node2WKT(node));
        osm.setIDType("osm");
        ArrayList type = new ArrayList();
        type.add("OSMEntity");
        type.add("OSMNode" );
        osm.setType(type);
        osm.setID(node.getId());
        osm.setOSMType(entity);
        osm.setSameAs(node.getTag());
        osm.setURI("http://openstreetmap.org/" + osm.getOSMType() + "/" + osm.getID());
        return osm;
    }
    public static OSM way2OSM(Way way, String nodePath) {
        String entity = "way";
        OSM osm = new OSM();
        osm.setLabel(way.getLabel());
        osm.setName_en(way.getName_en());
        osm.setName_zh(way.getName_zh());
        osm.setWKT(OSM2WKT.way2WKT(way, nodePath));
        osm.setIDType("osm");
        ArrayList type = new ArrayList();
        type.add("OSMEntity");
        type.add("OSMWay");
        osm.setType(type);
        osm.setID(way.getId());
        osm.setOSMType(entity);
        osm.setSameAs(way.getTag());
        osm.setURI("http://openstreetmap.org/" + osm.getOSMType() + "/" + osm.getID());
        return osm;
    }
    public static OSM relation2OSM(Relation relation, String nodePath, String wayPath, String relationPath) {
        String entity = "relation";
        OSM osm = new OSM();
        osm.setLabel(relation.getLabel());
        osm.setName_en(relation.getName_en());
        osm.setName_zh(relation.getName_zh());
        osm.setWKT(OSM2WKT.relation2WKT(relation, nodePath, wayPath, relationPath));
        osm.setIDType("osm");
        ArrayList type = new ArrayList();
        type.add("OSMEntity");
        type.add("OSMRelation");
        osm.setType(type);
        osm.setID(relation.getId());
        osm.setOSMType(entity);
        osm.setSameAs(relation.getTag());
        osm.setURI("http://openstreetmap.org/" + osm.getOSMType() + "/" + osm.getID());
        return osm;
    }
    public static Model RDF_OSM(Model model, OSM osm) {
        Model rdfmodel = model;
        String SameAS = "WikidataEntity";
        if(osm.getWKT() != null) {
            rdfmodel.createResource(osm.getURI())
                    .addProperty(OWL.sameAs,
                            rdfmodel.createResource("http://www.wikidata.org/wiki/" + osm.getSameAs())
                                    .addProperty(org.apache.jena.vocabulary.RDF.type, SameAS)
                                    .addProperty(VCARD.UID, "/wikidata/" + osm.getSameAs())
                    )
                    .addProperty(VCARD.GEO, osm.getWKT())
                    .addProperty(VCARD.CLASS, osm.getType().get(1))
                    .addProperty(RDFS.subClassOf, osm.getType().get(0))
                    .addProperty(org.apache.jena.vocabulary.RDF.type, osm.OSM_Type)
                    .addProperty(VCARD.UID, osm.getIDType() + "/" + osm.getID())
            ;
            if ( osm.getName_en() != "" && osm.getName_en() != null ) {
                rdfmodel.createResource(osm.getURI())
                        .addProperty(RDFS.label, rdfmodel.createLiteral(osm.getName_en(), "en"));
            } else if ( osm.getName_zh() != "" && osm.getName_zh() != null ) {
                rdfmodel.createResource(osm.getURI())
                        .addProperty(RDFS.label, rdfmodel.createLiteral(osm.getName_zh(), "zh"));
            } else {
                if (osm.getLabel() != "" && osm.getLabel() != null) {
                    rdfmodel.createResource(osm.getURI())
                            .addProperty(RDFS.label, osm.getLabel());
                }
            }
        }
        osm = null;
        return rdfmodel;
    }
    public static Model RDF_Wiki(Model model, OSM osm) {
        String entity = osm.getOSMType();
        Model rdfmodel = model;
        Wikidata wiki = new Wikidata();
        wiki.setWKT(osm.getWKT());
        wiki.setIDType("wikidata");
        ArrayList type = new ArrayList();
        type.add("WikidataEntity");
        wiki.setType(type);
        wiki.setID(osm.getSameAs());
        wiki.setLabel(osm.getLabel());
        wiki.setName_zh(osm.getName_zh());
        wiki.setName_en(osm.getName_en());
        wiki.setSameAs(osm.getID());
        wiki.setURI("http://www.wikidata.org/wiki/" + wiki.getID());
        String wkt = wiki.getWKT();
        if (wiki.getWKT() != null) {
            rdfmodel.createResource(wiki.getURI())
                    .addProperty(OWL.sameAs,
                            rdfmodel.createResource("http://openstreetmap.org/" + entity + "/" + wiki.getSameAs())
                                    .addProperty(VCARD.CLASS, osm.getType().get(1))
                                    .addProperty(RDFS.subClassOf, osm.getType().get(0))
                                    .addProperty(VCARD.UID, "/osm/" + entity + "/" + wiki.getSameAs())
                    )
                    .addProperty(VCARD.GEO, wkt)
                    .addProperty(org.apache.jena.vocabulary.RDF.type, wiki.getType().get(0))
                    .addProperty(VCARD.UID, wiki.getIDType() + "/" + wiki.getID())
            ;
            if ( wiki.getName_en() != "" && wiki.getName_en() != null ) {
                rdfmodel.createResource(wiki.getURI())
                        .addProperty(RDFS.label, rdfmodel.createLiteral(wiki.getName_en(), "en"));
            } else if ( wiki.getName_zh() != "" && wiki.getName_zh() != null ) {
                rdfmodel.createResource(wiki.getURI())
                        .addProperty(RDFS.label, rdfmodel.createLiteral(wiki.getName_zh(), "zh"));
            } else {
                if (wiki.getLabel() != "" && wiki.getLabel() != null) {
                    rdfmodel.createResource(wiki.getURI())
                            .addProperty(RDFS.label, wiki.getLabel());
                }
            }
        }
        wiki = null;
        return rdfmodel;
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

    public static void writeRDF(Model model, String RDFfile) {
//        File rdfFile = new File(RDFfile);
//        if (rdfFile.exists()) rdfFile.delete();
//        try {
//            rdfFile.createNewFile();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        //model.write(System.out, "RDF/XML-ABBREV");

        FileWriter out = null;
        try {
            out = new FileWriter(RDFfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        model.write(out, "RDF/XML-ABBREV");
    }

    public static void writeSelectedRDF(Model model, String RDFfile, String format) {
        // 这个函数是传入model，根据选择的输出format，生成RDFfile
        File file = new File(RDFfile);
        try {
            Writer out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream(file), "UTF-8"));
            model.write(out, format);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Model readRDF2model (String inputFileName) {
        // 这个函数是传入rdf文件，得到model
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

        return model;
    }

    public static void main(String args[]) {
        String rootPath = "F:/SmallApple/OSM-Wikidata_data/Result/";
        //ForServer
        //String rootPath = "/home/dsm/OSM-Wikidata/Result";
        String OSMFilePath_Taiwan = rootPath + "OSMwithWiki_Taiwan.osm";
        String OSMFilePath_China = rootPath + "OSMwithWiki_China.osm";
        RDF rdf = new RDF();
        //rdf.readOSM(OSMFilePath_Taiwan);
        //rdf.readOSM(OSMFilePath_China);
        /*
        String rootPath2 = "/home/dsm/OSM-Wikidata/Result_the end/";
        String format = "Turtle";
        String suffix = ".ttl";
        String rdfOSMFile_Taiwan = rootPath2 + "RDF_OSM_Taiwan(node&&way).xml";
        String rdfOSMFile_China = rootPath2 + "RDF_OSM_China(node).xml";
        String rdfOSMFile_ChinaALL = rootPath2 + "RDF_OSM_ChinaALL" + suffix;
        Model model_OSM = rdf.readRDF2model(rdfOSMFile_Taiwan).union(readRDF2model(rdfOSMFile_China));
        writeSelectedRDF(model_OSM, rdfOSMFile_ChinaALL, format);

        String rdfWikiFile_Taiwan = rootPath2 + "RDF_Wiki_Taiwan(node&&way).xml";
        String rdfWikiFile_China = rootPath2 + "RDF_Wiki_China(node).xml";
        String rdfWikiFile_ChinaALL = rootPath2 + "RDF_Wiki_ChinaALL" + suffix;
        Model model_Wiki = rdf.readRDF2model(rdfWikiFile_Taiwan).union(readRDF2model(rdfWikiFile_China));
        writeSelectedRDF(model_Wiki, rdfWikiFile_ChinaALL, format);
        */
        String OSMFilePath_Australia = "F:\\SmallApple\\OSM-Wikidata_data\\other\\OSMwithWiki_Australia.osm";
        rdf.readOSM(OSMFilePath_Australia);
    }
}
