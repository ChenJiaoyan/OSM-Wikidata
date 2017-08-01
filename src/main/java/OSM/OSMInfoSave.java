package OSM;

import FileHandle.HandleFiles;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static OSM_Wikidata.OSM2WKT.polygonOrPolyline;

/**
 * Created by SmallApple on 2017/4/15.
 */
public class OSMInfoSave  extends DefaultHandler {

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

    private String rootPath = "F:/SmallApple/OSM-Wikidata_data/Result/";
    //ForServer
    //private String rootPath = "/home/dsm/OSM-Wikidata/Result_the end/";
    private static String NodewithWikiPath = "F:\\NodePath_Taiwan(Wiki).txt";
    private static String WaywithWikiPath = "F:\\WayPath_Taiwan(Wiki).txt";
    private static String RelationwithWikiPath = "F:\\RelationPath_Taiwan(Wiki).txt";
    private static String NodePath = "F:\\NodePath_Taiwan.txt";
    private static String WayPath = "F:\\WayPath_Taiwan.txt";
    private static String RelationPath = "F:\\RelationPath_Taiwan.txt";

    private static String SplitStr = "--";

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
                    // 记录下存在wikidata链接的node的信息，备用
                    HandleFiles.WriteFile(NodewithWikiPath, n.getId() + SplitStr + n.getTag()
                            + SplitStr + n.getLabel() + SplitStr + n.getName_zh() + SplitStr + n.getName_en()
                            + SplitStr + n.getLon() + SplitStr + n.getLat()+ "\r\n");
                    System.out.println("Node Id: " + n.getId() + "\tName: " + kvcontents + "\tZh: " + kvcontents_Zh + "\tEn: " + kvcontents_En + "\tWiki: " + kvcontentsWiki);
                    n = null;
                }
            }
            //记录下所有node的 ID、经纬度信息，这是为了后面way、relation生成WKT格式数据做准备
            for (int i = 0; i < tagN; i++) {
                Nodes n = new Nodes();
                n = nodeslist.get(i);
                HandleFiles.WriteFile(NodePath, n.getId() + SplitStr + n.getLon() + SplitStr + n.getLat() + "\r\n");
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
            if(kvcontentsWiki != "") {
                way.setId(idcontents);
                way.setTag(kvcontentsWiki);
                way.setLabel(kvcontents);
                way.setName_en(kvcontents_En);
                way.setName_zh(kvcontents_Zh);
                way.setPointids(pointids);
                // 记录下存在wikidata链接的way的信息，备用
                HandleFiles.WriteFile(WaywithWikiPath, way.getId() + SplitStr + way.getTag() + SplitStr + way.getLabel()
                        + SplitStr + way.getName_en() + SplitStr + way.getName_zh() + SplitStr + way.getPointids() + "\r\n");
                System.out.println("Way Id:" + way.getId() + "\tName: " + kvcontents + "\tZh: " + kvcontents_Zh + "\tEn: " + kvcontents_En + "\tWiki: " + kvcontentsWiki);
                if (polygonOrPolyline(way.getPointids())) {
                    System.out.print("Polygon");
                } else {
                    System.out.print("Polyline");
                }
                System.out.println(way.getPointids());
            }
            //记录下所有way的 ID、引用node信息，这是为了后面way、relation生成WKT格式数据做准备
            HandleFiles.WriteFile(WayPath, way.getId() + SplitStr + way.getPointids() + "\r\n");

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
                // 记录下存在wikidata链接的relation的信息，备用
                HandleFiles.WriteFile(RelationwithWikiPath, relation.getId() + SplitStr + relation.getTag() + SplitStr +
                        relation.getLabel() + SplitStr + relation.getName_en() + SplitStr + relation.getName_zh() + SplitStr +
                        relation.getnodeIDs() + SplitStr + relation.getwayIDs() + SplitStr + relation.getrelationIDs() + "\r\n");
                System.out.println("Relation Id:" + relation.getId() + "\tName: " + kvcontents + "\tZh: " + kvcontents_Zh + "\tEn: " + kvcontents_En + "\tWiki: " + kvcontentsWiki);
                System.out.println(relation.getnodeIDs() + ", " + relation.getwayIDs() + ", " + relation.getrelationIDs());
            }
            //记录下所有relation的 ID、引用node/way/relation信息，这是为了后面relation生成WKT格式数据做准备
            HandleFiles.WriteFile(RelationPath, relation.getId() + SplitStr +
                    relation.getnodeIDs() + SplitStr + relation.getwayIDs() + SplitStr + relation.getrelationIDs() + "\r\n");

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
        System.out.println("Reading OSM/XML file is done!");
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

    public static void OSMFileSave(String filePath, String encode, String resultPath, String key) throws Exception {
        File file = new File(filePath);
        BufferedReader reader = null;
        String[] feature = {"node", "way", "relation"};
        int i;
        try {
            reader = new BufferedReader(new FileReader(file), 10 * 1024 * 1024);
            String stringLine = null;
            String newstr = null;
            for(i = 0; i < 3; i++) { //为了保持OSM文件的XML格式，最前面三行的内容需加上
                stringLine = reader.readLine();
                HandleFiles.WriteFile(resultPath, stringLine + "\r\n");
            }
            while ((stringLine = reader.readLine()) != null) {
                newstr = new String(stringLine.getBytes(encode), encode).trim();
                for(int r = 0; r < 1; r++) { //OSM文件一大，node和way、relation必须分开跑，要不然way和relation的记录不对，奇葩的问题！
                //for(int r = 1; r < 3; r++) {
                    if(newstr.indexOf("<" + feature[r] + " id=\"") >= 0) {
                        ArrayList strGroup = new ArrayList();
                        strGroup.add(stringLine + "\r\n");
                        while(newstr.indexOf("<tag k=\"" + key + "\" v=\"") < 0 && newstr.indexOf("</" + feature[r] + ">") < 0) {
                            stringLine = reader.readLine();
                            strGroup.add(stringLine + "\r\n");
                            newstr = new String(stringLine.getBytes(encode), encode).trim();
                        }
                        if(newstr.indexOf("<tag k=\"" + key + "\" v=\"") == 0) { //如果这些node id/way id/relation id确实有这个key
                            for(i = 0; i < strGroup.size(); i++) {
                                HandleFiles.WriteFile(resultPath, (String) strGroup.get(i));
                                System.out.print((String) strGroup.get(i));
                            }
                            while(newstr.indexOf("</" + feature[r] + ">") < 0) {
                                stringLine = reader.readLine();
                                HandleFiles.WriteFile(resultPath, stringLine + "\r\n");
                                System.out.println(stringLine);
                                newstr = new String(stringLine.getBytes(encode), encode).trim();
                            }
                        }
                        strGroup.clear();
                        break;
                    }
                }
                if(stringLine.equals("</osm>")) { //为了保持OSM文件的XML格式，最后一行的内容需加上
                    HandleFiles.WriteFile(resultPath, "</osm>");
                    break;
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        /*
        String filePathTaiwan = "F:\\taiwan-latest.osm";
        String encodeT = HandleFiles.getFileEncode(filePathTaiwan);
        String filePathChina = "F:\\china-latest.osm";
        String encodeC = HandleFiles.getFileEncode(filePathChina);
        String resultPathChina = "F:\\OSMwithWiki_China.osm";
        String resultPathTaiwan = "F:\\OSMwithWiki_Taiwan.osm";
        String nodePathChina = "F:\\OSMNode_China.txt";
        String nodePathTaiwan = "F:\\OSMNode_Taiwan.txt";
        String wayPathChina = "F:\\OSMWay_China.txt";
        String wayPathTaiwan = "F:\\OSMWay_Taiwan.txt";
        String relationPathChina = "F:\\OSMRelation_China.txt";
        String relationPathTaiwan = "F:\\OSMRelation_Taiwan.txt";
*/
        String rootPath = "F:\\SmallApple\\OSM-Wikidata_data\\Data\\OSM\\";
        String filePath = rootPath + "australia-latest.osm";
        String filePath2 = rootPath + "australia-latest2.osm";
        String encode = HandleFiles.getFileEncode(filePath);
        String rootPath2 = "F:\\SmallApple\\OSM-Wikidata_data\\other\\";
        String resultPath = rootPath2 + "OSMwithWiki_Australia.osm";
        String nodePath = rootPath2 + "OSMNode_Australia.txt";
        String wayPath = rootPath2 + "OSMWay_Australia.txt";
        String relationPath = rootPath2 + "OSMRelation_Australia.txt";

        String key = "wikidata";

        OSMInfoSave save = new OSMInfoSave();
        try {
            //OSMFileSave(filePathTaiwan, encodeT, resultPathTaiwan, nodePathTaiwan, wayPathTaiwan, relationPathTaiwan , key);
            //OSMFileSave(filePathChina, encodeC, resultPathChina, nodePathChina, wayPathChina, relationPathChina, key);
            //OSMFileSave2(filePathTaiwan, encodeT, resultPathTaiwan, key);
            //OSMFileSave2(filePathChina, encodeC, resultPathChina, key);
            //OSMFileSave(filePath, encode, resultPath, nodePath, wayPath, relationPath, key);
            //OSMFileSave2(filePath, encode, resultPath, key);
            //save.readOSM(filePath);
            OSMFileSave(filePath2, encode, resultPath, key);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
