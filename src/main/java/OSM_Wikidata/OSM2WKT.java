package OSM_Wikidata;

/**
 * Created by SmallApple on 2017/4/18.
 */

import FileHandle.HandleFiles;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;

public class OSM2WKT extends DefaultHandler {

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
    /**
     * 几何WKT字串
     */
    private final static String WKT_TAG_BEGIN_POINT = "POINT (";
    private final static String WKT_TAG_IBEGIN_POINT = "POINT";
    private final static String WKT_TAG_BEGIN_LINE = "LINESTRING (";
    private final static String WKT_TAG_IBEGIN_LINE = "LINESTRING";
    private final static String WKT_TAG_BEGIN_POL = "POLYGON (";
    private final static String WKT_TAG_IBEGIN_POL = "POLYGON";
    private final static String WKT_TAG_BEGIN_MULPOI = "MULTIPOINT (";
    private final static String WKT_TAG_IBEGIN_MULPOI = "MULTIPOINT";
    private final static String WKT_TAG_BEGIN_MULLINE = "MULTILINESTRING (";
    private final static String WKT_TAG_IBEGIN_MULLINE = "MULTILINESTRING";
    private final static String WKT_TAG_BEGIN_MULGON = "MULTIPOLYGON (";
    private final static String WKT_TAG_IBEGIN_MULGON = "MULTIPOLYGON";
    private final static String WKT_TAG_BEGIN_GEOCOL = "GEOMETRYCOLLECTION (";
    private final static String WKT_TAG_IBEGIN_GEOCOL = "GEOMETRYCOLLECTION";
    private final static String WKT_TAG_BRACK1 = "(";
    private final static String WKT_TAG_BRACK2 = ")";
    private final static String WKT_TAG_END = ")";
    private final static String WKT_TAG_BREAK = "\n";
    private final static String WKT_TAG_MARKADD = " ";
    private final static String WKT_TAG_MARKSEP1 = ",";
    private final static String WKT_TAG_MARKSEP2 = " ";

    static int precisonFloating = 3; // use 3 decimals after comma for rounding
    double epsilon = 0.0001;

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
    private String kvcontentsW = "";
    private String kvcontentsN = "";
    private String kvcontentsR = "";
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

    /**
     * 用HashMap进行存储内存会溢出，这是OSMtoWKT使用的方法
     * 需要另寻他法——先存到文件中保存起来，再进行匹配
     */
    /*
    private static String NodePath = "F:\\NodePath.txt";
    private static String WayPath = "F:\\WayPath.txt";
    private static String RelationPath = "F:\\RelationPath.txt";
    */
    //运行中国台湾的数据时：

    //(Wiki)
    private static String NodePath = "F:\\NodePath_Taiwan(Wiki).txt";
    private static String WayPath = "F:\\WayPath_Taiwan(Wiki).txt";
    private static String RelationPath = "F:\\RelationPath_Taiwan(Wiki).txt";

    //运行中国的数据时：
    /*
    //(Wiki)
    private static String NodePath = "F:\\NodePath(Wiki)_China.txt";
    private static String WayPath = "F:\\WayPath(Wiki)_China.txt";
    private static String RelationPath = "F:\\RelationPath(Wiki)_China.txt";
    */
    boolean Type = false, temTympe = false;
    private Integer countp = 0;
    private Integer countw = 0;
    private Integer countr = 0;
    private Integer counta = 0;
    //这仨值作为记录name的索引
    private Integer plagN = 0;
    private Integer plagW = 0;
    private Integer plagR = 0;
    //这tagN用于记录含有同一tag的节点数目，每次用完均要清零
    private Integer tagN = 0;
    //tagIF作为记录含有同一tag key&&value的判断参数
    private Integer tagIF = 0;

    private static String SplitStr = "--";

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
                    nodeslist.get(i).setTag(kvcontentsN);
                    Nodes n = new Nodes();
                    n = nodeslist.get(i);
                    System.out.println("Node Id: " + n.getId() + "\tName: " + n.getTag());
                    HandleFiles.WriteFile(NodePath, n.getId() + SplitStr + n.getLon() + SplitStr + n.getLat() + SplitStr + n.getTag() + "\r\n");
                }
                nodeslist.clear();;
                kvcontentsN = "";
                curretntag = "";
                plagN = 0;
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
            countp++;
            tagN++;
            nodes = new Nodes();
            nodes.setId(idcontents);
            nodes.setLon(loncontents);
            nodes.setLat(latcontents);
            nodes.setTag(kvcontentsN);
            nodeslist.add(nodes);
        }

        if ("node".equals(curretntag) && "tag".equals(qName) && plagN == 0) {
            tagIF = 1;
            String kcontents = attributes.getValue("k");
            String vcontents = attributes.getValue("v");
            kvcontentsN = kcontents + "-" + vcontents; //这是在没有name的情况下，记录下一个key及其value作为tag
            if(kcontents.equals(XML_TAG_NAME) || kcontents.equals(XML_TAG_NAME + ":zh")) { //提取出OSM实体node的name.如果有中文名，就记录下中文名
                plagN = 1;
                //kvcontentsN = kcontents + "=" + vcontents;
                kvcontentsN = vcontents;
            }
            kvcontentsN.replace("&#10", " ");
            kvcontentsN.replace("&#13", " ");
        }
        if("node".equals(curretntag) && "tag".equals(qName) && attributes.getValue("k").equals(XML_TAG_NAME + ":zh")) {
            String kcontents = attributes.getValue("k");
            String vcontents = attributes.getValue("v");
            plagN = 1;
            //kvcontentsN = kcontents + "=" + vcontents;
            kvcontentsN = vcontents;
            kvcontentsN.replace("&#10", " ");
            kvcontentsN.replace("&#13", " ");
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
                    nodeslist.get(i).setTag(kvcontentsN);
                    Nodes n = new Nodes();
                    n = nodeslist.get(i);
                    System.out.println("Node Id: " + n.getId() + "\tName: " + n.getTag());
                    HandleFiles.WriteFile(NodePath, n.getId() + SplitStr + n.getLon() + SplitStr + n.getLat() + SplitStr + n.getTag() + "\r\n");
                }
                nodeslist.clear();;
                kvcontentsN = "";
                curretntag = "";
                plagN = 0;
                tagN = 0;
                tagIF = 0;
                nodes = null;
            }
            //再对way进行操作
            way = new Way();
            pointids = new Vector<String>();
            points = new Vector<Nodes>();
            if (attributes.getValue("id") != null && attributes.getValue("id") != "")
                idcontents = attributes.getValue("id");
            else
                idcontents = "0";
            curretntag = "way";
            countw++;
        }

        if ("way".equals(curretntag) && "tag".equals(qName) && plagW == 0) {
            String kcontents = attributes.getValue("k");
            String vcontents = attributes.getValue("v");
            kvcontentsW = kcontents + "-" + vcontents;//这是在没有name的情况下，记录下一个key及其value作为tag
            if(kcontents.equals(XML_TAG_NAME)) { //提取出OSM实体way的name
                plagW = 1;
                //kvcontentsW = kcontents + "=" + vcontents;
                kvcontentsW = vcontents;
            }
            kvcontentsW.replace("&#10", " ");
            kvcontentsW.replace("&#13", " ");
        }
        if("way".equals(curretntag) && "tag".equals(qName) && attributes.getValue("k").equals(XML_TAG_NAME + ":zh")) {
            String kcontents = attributes.getValue("k");
            String vcontents = attributes.getValue("v");
            plagW = 1;
            //kvcontentsW = kcontents + "=" + vcontents;
            kvcontentsW = vcontents;
            kvcontentsW.replace("&#10", " ");
            kvcontentsW.replace("&#13", " ");
        }

        if ("way".equals(curretntag) && "nd".equals(qName)) { //对way的引用node操作
            String ref = attributes.getValue("ref");
            pointids.add(ref);
        }

        //对relation操作
        if ("relation".equals(qName)) {
            nodeIDs = new Vector<String>();
            wayIDs = new Vector<String>();
            relationIDs = new Vector<String>();
            if (attributes.getValue("id") != null && attributes.getValue("id") != "")
                idcontents = attributes.getValue("id");
            else
                idcontents = "0";
            curretntag = "relation";
            countr++;
        }

        if ("relation".equals(curretntag) && "tag".equals(qName) && plagR == 0) {
            String kcontents = attributes.getValue("k");
            String vcontents = attributes.getValue("v");
            kvcontentsR = kcontents + "-" + vcontents;//这是在没有name的情况下，记录下一个key及其value作为tag
            if(kcontents.equals(XML_TAG_NAME)) { //提取出OSM实体relation的name
                plagR = 1;
                //kvcontentsR = kcontents + "=" + vcontents;
                kvcontentsR = vcontents;
            }
            kvcontentsR.replace("&#10", " ");
            kvcontentsR.replace("&#13", " ");
        }
        if("relation".equals(curretntag) && "tag".equals(qName) && attributes.getValue("k").equals(XML_TAG_NAME + ":zh")) {
            String kcontents = attributes.getValue("k");
            String vcontents = attributes.getValue("v");
            //kvcontentsR = kcontents + "=" + vcontents;
            kvcontentsR = vcontents;
            plagR = 1;
            kvcontentsR.replace("&#10", " ");
            kvcontentsR.replace("&#13", " ");
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
            kvcontentsW.replace("&#10", " ");
            kvcontentsW.replace("&#13", " ");
            way.setId(idcontents);
            way.setTag(kvcontentsW);
            way.setPointids(pointids);
            System.out.println("Way Id:" + way.getId() + "\tName: " + way.getTag());
            if (polygonOrPolyline(way.getPointids())) {
                System.out.print("Polygon");
            } else {
                System.out.print("Polyline");
            }
            System.out.println(way.getPointids());
            HandleFiles.WriteFile(WayPath, way.getId() + SplitStr + way.getTag() + SplitStr + way.getPointids() + "\r\n");
            kvcontentsW = "";
            curretntag = "";
            pointids = null;
            plagW = 0;
            way = null;
        }

        //对relation进行处理
        if ("relation".equals(qName)) {
            kvcontentsR.replace("&#10", " ");
            kvcontentsR.replace("&#13", " ");
            relation = new Relations();
            relation.setId(idcontents);
            relation.setTag(kvcontentsR);
            relation.setnodeIDs(nodeIDs);
            relation.setwayIDs(wayIDs);
            relation.setrelationIDs(relationIDs);
            System.out.println("Relation Id:" + relation.getId() + "\tName: " + relation.getTag());
            System.out.println(relation.getnodeIDs() + ", " + relation.getwayIDs() + ", " + relation.getrelationIDs());
            HandleFiles.WriteFile(RelationPath, relation.getId() + SplitStr + relation.getTag() + SplitStr +
                    relation.getnodeIDs() + SplitStr + relation.getwayIDs() + SplitStr + relation.getrelationIDs() + "\r\n");
            kvcontentsR = "";
            curretntag = "";
            nodeIDs = null;
            wayIDs = null;
            relationIDs = null;
            plagR = 0;
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
                        Type = true; //polygon;
                    else
                        Type = false;
                    if(Type == true){
                        System.out.println("Polygon" + waylist.get(i).getPointids());
                    }
                    if(Type == false){
                        System.out.println("Polyline" + waylist.get(i).getPointids());
                    }
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

    public static boolean isNumeric(String str) {
        for (int i = str.length();--i>=0;){
            if (!Character.isDigit(str.charAt(i))){
                return false;
            }
        }
        return true;
    }

    private static String[] getSplit(String str) {
        String[] s = new String[5];
        int i = 0;
        int j;
        int n = 0;
        while((j = str.indexOf(SplitStr, i)) >= 0) {
            String ss = new String(str.substring(i,j));
            s[n] = ss;
            //System.out.println(s[n]);
            i = j + SplitStr.length();
            n++;
        }
        if(i <= str.length()-1) {
            String ss = new String(str.substring(i, str.length()));
            s[n] = ss;
        }
        return s;

    }

    public static Nodes getNode(String nodeLine) {
        if(nodeLine == null) {
            return null;
        }
        Nodes node = new Nodes();
        //String[] nodeInf = nodeLine.split(SplitStr);
        String[] nodeInf = getSplit(nodeLine);
        /**解释一下？
         * 这里使用split函数会导致内存溢出，因为在使用getNode()函数时，需要多次扫描node数据文件
         * 而split函数最终调用的是String类的substring方法，
         * substring出的来String小对象，仍然会指向原String大对象的char[]，
         * 为了避免内存拷贝，提高性能，substring并没有重新创建char数组，而是直接复用了原String对象的char[]，通过改变偏移。
         * 因此存在同样的问题。split出来的小对象，直接使用原String对象的char[]
         * 所以就导致了OutOfMemoryError问题
         */
        if(!isNumeric(nodeInf[0]) || nodeInf.length < 4) {
            return null;
        }
        node.setId(nodeInf[0]);
        node.setLon(nodeInf[1]);
        node.setLat(nodeInf[2]);
        node.setTag(nodeInf[3]);
        nodeInf = null;
        return node;
    }
    public static Nodes getNodebyID (String nodeid, String nodePath) {
        File file = new File(nodePath);
        BufferedReader reader = null;
        Nodes node = new Nodes();
        try {
            reader = new BufferedReader(new FileReader(nodePath), 10 * 1024 * 1024);
            String stringLine = null;
            while ((stringLine = reader.readLine()) != null) {
                String[] nodeInf = stringLine.split(SplitStr);
                if(nodeInf.length < 4) continue;
                String id = nodeInf[0];
                if(nodeid.equals(id)) {
                    node.setId(id);
                    node.setLon(nodeInf[1]);
                    node.setLat(nodeInf[2]);
                    node.setTag(nodeInf[3]);
                    /*node = OSM2WKT.getNode(stringLine);
                    if(node == null) {
                        continue;
                    }*/
                    break;
                }
            }
            reader.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        //System.out.println(node.getId() + "," + node.getTag() + "," + node.getLon() + " " + node.getLat());
        return node;
    }


    public static Way getWay(String wayline) {
        if(wayline == null) {
            return null;
        }
        Way way = new Way();
        //String[] wayInf = wayline.split(SplitStr);
        String[] wayInf = getSplit(wayline);
        if(!isNumeric(wayInf[0])) {
            return null;
        }
        Vector<String> noderef = new Vector<String>();
        if(wayInf[2].length() > 2) {
            String nodeset = wayInf[2].substring(1, wayInf[2].length() - 1);
            if(nodeset.indexOf(",") < 0) {
                noderef.add(nodeset.trim());
            }
            String[] nodes = nodeset.split(",");
            if(nodes.length > 1) {
                for (int i = 0; i < nodes.length; i++) {
                    noderef.add(nodes[i].trim());
                }
            }
        } else {
            noderef = null;
        }
        way.setId(wayInf[0]);
        way.setPointids(noderef);
        way.setTag(wayInf[1]);
        return way;
    }
    public static Way getWaybyID (String wayid, String wayPath) {
        File file = new File(wayPath);
        BufferedReader reader = null;
        Way way = new Way();
        try {
            reader = new BufferedReader(new FileReader(wayPath), 10 * 1024 * 1024);
            String stringLine = null;
            while ((stringLine = reader.readLine()) != null) {
                String[] wayInf = stringLine.split(SplitStr);
                if(wayInf.length < 3) continue;
                String id = wayInf[0];
                if(wayid.equals(id)) {
                    Vector<String> noderef = new Vector<>();
                    if(wayInf[2].length() > 2) {
                        String nodeset = wayInf[2].substring(1, wayInf[2].length()-1);
                        if(nodeset.indexOf(",") < 0) {
                            noderef.add(nodeset.trim());
                        }
                        String[] nodes = nodeset.split(",");
                        if(nodes.length > 1) {
                            for (int i = 0; i < nodes.length; i++) {
                                noderef.add(nodes[i].trim());
                            }
                        }
                    } else {
                        noderef = null;
                    }
                    way.setId(id);
                    way.setTag(wayInf[1]);
                    way.setPointids(noderef);
                    break;
                }
            }
            reader.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return way;
    }

    public static Relations getRelation(String relationline) {
        if(relationline == null) {
            return null;
        }
        Relations relation = new Relations();
        //String[] relationInf = relationline.split(SplitStr);
        String[] relationInf = getSplit(relationline);
        if(!isNumeric(relationInf[0])) {
            return null;
        }
        Vector<String> noderef = new Vector<String>();
        Vector<String> wayref = new Vector<String>();
        Vector<String> relationref = new Vector<String>();
        if(relationInf[2].length() > 2) {
            String nodeset = relationInf[2].substring(1, relationInf[2].length()-1);
            if(nodeset.indexOf(",") < 0) {
                noderef.add(nodeset.trim());
            }
            String[] nodes = nodeset.split(",");
            if(nodes.length > 1) {
                for (int i = 0; i < nodes.length; i++) {
                    noderef.add(nodes[i].trim());
                }
            }
        } else {
            noderef = null;
        }
        if(relationInf[3].length() > 2) {
            String wayset = relationInf[3].substring(1, relationInf[3].length()-1);
            if(wayset.indexOf(",") < 0) {
                wayref.add(wayset.trim());
            }
            String[] ways = wayset.split(",");
            if(ways.length > 1) {
                for (int i = 0; i < ways.length; i++) {
                    wayref.add(ways[i].trim());
                }
            }
        } else {
            wayref = null;
        }
        if(relationInf[4].length() > 2) {
            String relationset = relationInf[4].substring(1, relationInf[4].length()-1);
            if(relationset.indexOf(",") < 0) {
                relationref.add(relationset.trim());
            }
            String[] relations = relationset.split(",");
            if(relations.length > 1) {
                for (int i = 0; i < relations.length; i++) {
                    relationref.add(relations[i].trim());
                }
            }
        } else {
            relationref = null;
        }
        relation.setId(relationInf[0]);
        relation.setTag(relationInf[1]);
        relation.setnodeIDs(noderef);
        relation.setwayIDs(wayref);
        relation.setrelationIDs(relationref);
        return relation;
    }
    public static Relations getRelationByID(String relationID, String relationPath) {
        if(relationID == null || !isNumeric(relationID)) {
            return null;
        }
        Relations relation = new Relations();
        Vector<String> noderef = new Vector<String>();
        Vector<String> wayref = new Vector<String>();
        Vector<String> relationref = new Vector<String>();
        File file = new File(relationPath);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(relationPath), 10 * 1024 * 1024);
            String stringLine = null;
            while ((stringLine = reader.readLine()) != null) {
                String[] relationInf = stringLine.split(SplitStr);
                if(relationInf.length < 5) continue;
                String id = relationInf[0];
                if(relationID.equals(id)) {
                    if(relationInf[2].length() > 2) {
                        String nodeset = relationInf[2].substring(1, relationInf[2].length()-1);
                        if(nodeset.indexOf(",") < 0) {
                            noderef.add(nodeset.trim());
                        }
                        String[] nodes = nodeset.split(",");
                        if(nodes.length > 1) {
                            for (int i = 0; i < nodes.length; i++) {
                                noderef.add(nodes[i].trim());
                            }
                        }
                    } else {
                        noderef = null;
                    }
                    if(relationInf[3].length() > 2) {
                        String wayset = relationInf[3].substring(1, relationInf[3].length()-1);
                        if(wayset.indexOf(",") < 0) {
                            wayref.add(wayset.trim());
                        }
                        String[] ways = wayset.split(",");
                        if(ways.length > 1) {
                            for (int i = 0; i < ways.length; i++) {
                                wayref.add(ways[i].trim());
                            }
                        }
                    } else {
                        wayref = null;
                    }
                    if(relationInf[4].length() > 2) {
                        String relationset = relationInf[4].substring(1, relationInf[4].length()-1);
                        if(relationset.indexOf(",") < 0) {
                            relationref.add(relationset.trim());
                        }
                        String[] relations = relationset.split(",");
                        if(relations.length > 1) {
                            for (int i = 0; i < relations.length; i++) {
                                relationref.add(relations[i].trim());
                            }
                        }
                    } else {
                        relationref = null;
                    }
                    relation.setId(id);
                    relation.setTag(relationInf[1]);
                    relation.setnodeIDs(noderef);
                    relation.setwayIDs(wayref);
                    relation.setrelationIDs(relationref);
                    break;
                }
            }
            reader.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return relation;
    }

    public static boolean polygonOrPolyline(Vector<String> nodes) {
        //用于区分线和面数据
        //If return true, the poly is a polygon.
        if (nodes == null)
            return false;
        int size = nodes.size();
        if (size < 4)
            return false;
        if ( nodes.get(0).equals(nodes.get(size-1)) )
            return true;
        else
            return false;
    }


    public static double round(double d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Double.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
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

    //****************************************************************************
    // taken from source code of ONE simulator
    protected void skipUntil(Reader r, char until) throws IOException {
        char c;
        do {
            c = (char) r.read();
        } while (c != until && c != (char) -1);
    }

    public String readNestedContents(Reader r) throws IOException {
        StringBuffer contents = new StringBuffer();
        int parOpen; // nrof open parentheses
        char c = '\0';
        skipUntil(r, '(');
        parOpen = 1;
        while (c != (char) -1 && parOpen > 0) {
            c = (char) r.read();
            if (c == '(') {
                parOpen++;
            }
            if (c == ')') {
                parOpen--;
            }
            if (Character.isWhitespace(c)) {
                c = ' '; // convert all whitespace to basic space
            }
            contents.append(c);
        }
        contents.deleteCharAt(contents.length() - 1);    // remove last ')'
        return contents.toString();
    }

    //****************************************************************************
    private boolean readWkt(String filePath) {
        System.out.println("reading in wkt format ...");
        try {
            // check is file exists
            File file = new File(filePath);
            if (!file.exists()) {
                System.out.println("wkt file " + filePath + " does not exist");
                return false;
            }
            FileReader freader = new FileReader(file);
            BufferedReader reader = new BufferedReader(freader);
            long nodeid = 0;
            long wayid = 0;
            while (reader.ready()) {
                String line = readNestedContents(reader).trim();
                if (line.length() == 0) continue;
                String parts[] = line.split(WKT_TAG_MARKSEP1);
                Vector<String> street = new Vector<String>();
                for (String item : parts) {
                    item = item.trim();
                    String onetwo[] = item.split(WKT_TAG_MARKSEP2);
                    if (onetwo.length != 2) {
                        System.out.println("invalid coordinate pair: " + item);
                        continue;
                    }
                    double x = Double.parseDouble(onetwo[0]);
                    double y = Double.parseDouble(onetwo[1]);
                    // known node or new one?
                    long currentmark = -1;
                    File nodefile = new File(NodePath);
                    BufferedReader nodereader = null;
                    try {
                        nodereader = new BufferedReader(new FileReader(nodefile), 10 * 1024 * 1024);
                        String s = null;
                        while ((s = nodereader.readLine()) != null) {
                            Nodes n = new Nodes();
                            n = getNode(s);
                            if(n == null) {
                                continue;
                            }
                            Double nodex = Double.valueOf(n.getLon());
                            Double nodey = Double.valueOf(n.getLat());
                            if (Math.abs(nodex - x) < epsilon && Math.abs(nodey - y) < epsilon) {
                                currentmark = Long.parseLong(s);
                                break;
                            }
                        }
                        nodereader.close();
                    } catch (FileNotFoundException e) {
                    }

                    // need to generate new node
                    if (currentmark == -1) {
                        Nodes nd = new Nodes();
                        currentmark = nodeid++;
                        nd.setId(Long.toString(currentmark));
                        nd.setLon(Double.toString(x));
                        nd.setLat(Double.toString(y));
                        nd.setTag("New Generated Node");
                        HandleFiles.WriteFile(NodePath, nd.getId() + SplitStr + nd.getLon() + SplitStr + nd.getLat() + SplitStr + nd.getTag() + "\r\n");
                        countp++;
                        street.add(Long.toString(currentmark));
                    } else
                        street.add(Long.toString(currentmark));
                } //for(String item : parts)
                HandleFiles.WriteFile(WayPath, String.valueOf(wayid++) + "New Generated Way" + SplitStr + street + SplitStr + "\r\n");
                countw++;
            }
        } catch (Exception e) {
            System.out.println("reading wkt file failed: " + e.getLocalizedMessage());
            e.printStackTrace();
            return false;
        }
        System.out.println("parsing wkt found " + countw + " streets");
        return true;
    }

    private boolean transformCoordinates() {
        // in this function we have to restrict the precision we calculate the x, y coordinates of the point to avoid floating point errors
        System.out.println("transforming geographic landmarks ...");
        // search for top,bottom,left,right marks
        // latitude is horizontal from -90 -- 0 -- 90
        // longitude is vertical from -180 -- 0 -- 180
        double latMin, latMax, lonMin, lonMax;
        // initialize switched to move to value in for loop
        latMin = 90;
        latMax = -90;
        lonMin = 180;
        lonMax = -180;
        // search for geographic bounds
        File nodefile = new File(NodePath);
        BufferedReader nodereader = null;
        try {
            nodereader = new BufferedReader(new FileReader(nodefile), 10 * 1024 * 1024);
            String s = null;
            while ((s = nodereader.readLine()) != null) {
                Nodes l = new Nodes();
                l = getNode(s);
                if(l == null) {
                    continue;
                }
                if (Double.parseDouble(l.getLat()) < latMin)
                    latMin = Double.parseDouble(l.getLat());
                if (Double.parseDouble(l.getLat()) > latMax)
                    latMax = Double.parseDouble(l.getLat());
                if (Double.parseDouble(l.getLon()) < lonMin)
                    lonMin = Double.parseDouble(l.getLon());
                if (Double.parseDouble(l.getLon()) > lonMax)
                    lonMax = Double.parseDouble(l.getLon());
            }
            nodereader.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("found geographic bounds:"
                + " latitude from " + latMin + " to " + latMax
                + " longitude from " + lonMin + " to " + lonMax);
        double width = Double.parseDouble(geoDistance(latMin, lonMin, latMin, lonMax));
        double height = Double.parseDouble(geoDistance(latMin, lonMin, latMax, lonMin));
        System.out.println("geographic area dimensions are: height " + height + "m, width " + width + "m");
        // put coordinate system to upper left corner with (0,0), output in meters
        File nodefile1 = new File(NodePath);
        BufferedReader nodereader1 = null;
        try {
            nodereader1 = new BufferedReader(new FileReader(nodefile1), 10 * 1024 * 1024);
            String s = null;
            while ((s = nodereader1.readLine()) != null) {
                Nodes l = new Nodes();
                l = getNode(s);
                if(l == null) {
                    continue;
                }
                l.setLon(geoDistance(Double.parseDouble(l.getLat()), Double.parseDouble(l.getLon()), Double.parseDouble(l.getLat()), lonMin));
                l.setLat(geoDistance(Double.parseDouble(l.getLat()), Double.parseDouble(l.getLon()), latMin, Double.parseDouble(l.getLon())));
            }
            nodereader1.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private double plainDistance(Nodes mark1, Nodes mark2) {
        return plainDistance(Double.parseDouble(mark1.getLon()), Double.parseDouble(mark1.getLat()), Double.parseDouble(mark2.getLon()), Double.parseDouble(mark2.getLat()));
    }

    private double plainDistance(double x1, double y1, double x2, double y2) {
        double x = x1 - x2;
        double y = y1 - y2;
        double distance = Math.sqrt(x * x + y * y);
        distance = OSM2WKT.round(distance, OSM2WKT.precisonFloating);
        return distance;
    }

    private String geoDistance(double lat1, double lon1, double lat2, double lon2) {
        // return distance between two gps fixes in meters
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = (R * c * 1000.0d);
        distance = OSM2WKT.round(distance, OSM2WKT.precisonFloating);
        return String.valueOf(distance);
    }

    private boolean translate(int x, int y) {
        if (x == 0 && y == 0) return true;
        System.out.println("translating map by x=" + x + " and y=" + y);
        File nodefile = new File(NodePath);
        BufferedReader nodereader = null;
        try {
            nodereader = new BufferedReader(new FileReader(nodefile), 10 * 1024 * 1024);
            String s = null;
            while ((s = nodereader.readLine()) != null) {
                Nodes mark = new Nodes();
                mark = getNode(s);
                if(mark == null) {
                    continue;
                }
                mark.setLon( String.valueOf( Long.parseLong(mark.getLon()) + x ) );
                mark.setLat( String.valueOf( Long.parseLong(mark.getLat()) + x ) );
            }
            nodereader.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("translation done");
        return true;
    }

    public static String node2WKT(Nodes node) {
        return WKT_TAG_BEGIN_POINT + node.getLon() + WKT_TAG_MARKADD + node.getLat() + WKT_TAG_END;
    }

    public static String way2WKT(Way way, String nodePath) {
        String str = "";
        Vector<String> s = way.getPointids();
        if(s == null) {
            return "";
        }
        if(polygonOrPolyline(s)) {
            str += WKT_TAG_BEGIN_POL;
        } else {
            str += WKT_TAG_BEGIN_LINE;
        }
        for (int i = 0; i < s.size(); i++) {
            String l = s.elementAt(i);
            Nodes mark = getNodebyID(l, nodePath);
            str += mark.getLon() + WKT_TAG_MARKADD + mark.getLat();
            if (i + 1 < s.size()) {
                str += WKT_TAG_MARKSEP1 + WKT_TAG_MARKSEP2;
            }
        }
        str += WKT_TAG_END + WKT_TAG_BREAK;
        return str;
    }
    private static Vector getNoSameObjectVector(Vector vector){
        Vector tempVector = new Vector();
        HashSet set = new HashSet(vector);
        //addAll(Collection c);  //可以接受Set和List类型的参数
        tempVector.addAll(set);
        return tempVector;

    }
    public static String relation2WKT(Relations relation, String nodePath, String wayPath, String relationPath) {
        System.out.println("Before:" + relation.getnodeIDs() + "," + relation.getwayIDs());
        String str = "";
        Vector<String> noder = relation.getnodeIDs();
        Vector<String> wayr = relation.getwayIDs();
        if(relation.getrelationIDs() != null) {
            for (int i = 0; i < relation.getrelationIDs().size(); i++) {
                String sr = relation.getrelationIDs().elementAt(i);
                /**
                 * 很神奇，在osm文件里relation的relation reference中，找不到对应的node id或way id
                 * 比如说，relation,270056,中国,Q148，有<member type="relation" ref="913011" role="subarea"/>这条reference，
                 * 但是在osm文件里搜索不到id为913011的relation、
                 */
                Relations re = getRelationByID(sr, relationPath);
                if (re != null && re.getnodeIDs() != null) {
                    for (int j = 0; j < re.getnodeIDs().size(); j++) {
                        noder.add(re.getnodeIDs().elementAt(j));
                    }
                }
                if (re != null && re.getwayIDs() != null) {
                    for (int k = 0; k < re.getwayIDs().size(); k++) {
                        wayr.add(re.getwayIDs().elementAt(k));
                    }
                }
            }
            getNoSameObjectVector(noder);
            getNoSameObjectVector(wayr);
            relation.setnodeIDs(noder);
            relation.setwayIDs(wayr);
        }
        //System.out.println(relation.getId() + relation.getTag() + relation.getnodeIDs() + "," + relation.getwayIDs());
        //System.out.println(relation.getnodeIDs() == null);
        // 如果经过处理的relation只有node的reference
        System.out.println("After:" + relation.getnodeIDs() + "," + relation.getwayIDs());
        if(relation.getnodeIDs() != null  && relation.getwayIDs() == null) {
            if(relation.getnodeIDs().size() == 1) {
                Nodes node = getNodebyID(relation.getnodeIDs().elementAt(0), nodePath);
                str += WKT_TAG_BEGIN_POINT + node.getLon() + WKT_TAG_MARKADD + node.getLat() + WKT_TAG_END;
            } else {
                str += WKT_TAG_BEGIN_MULPOI;
                for(int i = 0; i < relation.getnodeIDs().size(); i++) {
                    Nodes node = getNodebyID(relation.getnodeIDs().elementAt(i), nodePath);
                    str += node.getLon() + WKT_TAG_MARKADD + node.getLat();
                    if (i + 1 < relation.getnodeIDs().size()) {
                        str += WKT_TAG_MARKSEP1 + WKT_TAG_MARKSEP2;
                    }
                }
                str += WKT_TAG_END;
            }
        }
        // 如果经过处理的relation只有way的reference
        if(relation.getnodeIDs() == null && relation.getwayIDs() != null) {
            int p = 0; //判断way的集合中是否有POINT的WKT格式
            int l = 0; //判断way的集合中是否有LINESTRING的WKT格式
            int g = 0; //判断way的集合中是否有POLYGON的WKT格式
            if(relation.getwayIDs().size() == 1) {
                Way way = getWaybyID(relation.getwayIDs().elementAt(0), wayPath);
                str += way2WKT(way, wayPath);
            } else if(relation.getwayIDs().size() > 1) {
                Way way = new Way();
                String[] wayset = new String[relation.getwayIDs().size() + 1];
                for(int i=0; i<relation.getwayIDs().size(); i++) {
                    way = getWaybyID(relation.getwayIDs().elementAt(i), wayPath);
                    wayset[i] = way2WKT(way, wayPath);
                    if(wayset[i].indexOf(WKT_TAG_BEGIN_POINT) == 0) p = 1;
                    if(wayset[i].indexOf(WKT_TAG_BEGIN_LINE) == 0) l = 1;
                    if(wayset[i].indexOf(WKT_TAG_BEGIN_POL) == 0) g = 1;
                }
                if( p + l + g > 1) {
                    str += WKT_TAG_BEGIN_GEOCOL;
                    for(int i=0; i<relation.getwayIDs().size(); i++) {
                        str += wayset[i];
                        if(i+1 < relation.getwayIDs().size()) {
                            str += WKT_TAG_MARKSEP1 + WKT_TAG_MARKSEP2;
                        }
                    }
                    str += WKT_TAG_END;
                } else {
                    String strtype = "";
                    if(p == 1) strtype = WKT_TAG_BEGIN_MULPOI;
                    if(l == 1) strtype = WKT_TAG_BEGIN_MULLINE;
                    if(g == 1) strtype = WKT_TAG_BEGIN_MULGON;
                    str += strtype;
                    for(int i=0; i<relation.getwayIDs().size(); i++) {
                        str += wayset[i].substring(wayset[i].indexOf("("));
                        if(i+1 < relation.getwayIDs().size()) {
                            str += WKT_TAG_MARKSEP1 + WKT_TAG_MARKSEP2;
                        }
                    }
                    str += WKT_TAG_END;
                }
            }
        }
        // 如果经过处理的relation不仅有node，也有way的reference
        if(relation.getnodeIDs() != null && relation.getwayIDs() != null) {
            str += WKT_TAG_BEGIN_GEOCOL;
            if(relation.getnodeIDs().size() == 1) {
                Nodes node = getNodebyID(relation.getnodeIDs().elementAt(0), nodePath);
                str += WKT_TAG_BEGIN_POINT + node.getLon() + WKT_TAG_MARKADD + node.getLat();
            } else if (relation.getnodeIDs().size() > 1) {
                str += WKT_TAG_BEGIN_MULPOI;
                for(int i = 0; i < relation.getnodeIDs().size(); i++) {
                    Nodes node = getNodebyID(relation.getnodeIDs().elementAt(i), nodePath);
                    str += node.getLon() + WKT_TAG_MARKADD + node.getLat();
                    if(i + 1 <  relation.getnodeIDs().size()) {
                        str += WKT_TAG_MARKSEP1 + WKT_TAG_MARKSEP2;
                    }
                }
            }

            str += WKT_TAG_END + WKT_TAG_MARKSEP1 + WKT_TAG_MARKSEP2;

            if(relation.getwayIDs().size() == 1) {
                Way way = getWaybyID(relation.getwayIDs().elementAt(0), wayPath);
                str += way2WKT(way, wayPath);
            } else if(relation.getwayIDs().size() > 1) {
                Way way = new Way();
                String[] wayset = new String[relation.getwayIDs().size() + 1];
                for (int i = 0; i < relation.getwayIDs().size(); i++) {
                    way = getWaybyID(relation.getwayIDs().elementAt(i), WayPath);
                    str += way2WKT(way, wayPath);
                    if(i+1 < relation.getwayIDs().size()) {
                        str += WKT_TAG_MARKSEP1 + WKT_TAG_MARKSEP2;
                    }
                }
            }
            str += WKT_TAG_END;
        }
        //System.out.println(str);
        return str;
    }

    public boolean writeWkt(String wktfile, String feature, String nodePath, String wayPath, String relationPath) {
        System.out.println("writing wkt file ...");
        try {
            File wkt = new File(wktfile);
            if (wkt.exists()) wkt.delete();
            wkt.createNewFile();
            if(feature.equals("node")) {
                File nodefile = new File(nodePath);
                BufferedReader nodereader = null;
                try {
                    nodereader = new BufferedReader(new FileReader(nodefile), 10 * 1024 * 1024);
                    String ss = null;
                    while((ss = nodereader.readLine()) != null) {
                        Nodes n = new Nodes();
                        n = getNode(ss);
                        if(n == null) {
                            continue;
                        }
                        System.out.println(n.getId() + "," + n.getTag() + "," + node2WKT(n));
                        HandleFiles.WriteFile(wktfile, n.getId() + SplitStr + n.getTag() + SplitStr + node2WKT(n) + "\r\n");
                    }
                    nodereader.close();
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if(feature.equals("way")) {
                File wayfile = new File(wayPath);
                BufferedReader wayreader = null;
                try {
                    wayreader = new BufferedReader(new FileReader(wayfile), 10 * 1024 * 1024);
                    String ss = null;
                    while ((ss = wayreader.readLine()) != null) {
                        Way w = new Way();
                        w = getWay(ss);
                        if(w == null) {
                            continue;
                        }
                        System.out.println(w.getId() + w.getTag() + w.getPointids() + "\n" + way2WKT(w, wayPath));
                        HandleFiles.WriteFile(wktfile, w.getId() + SplitStr + w.getTag() + SplitStr + way2WKT(w, wayPath) + "\r\n");
                    }
                    wayreader.close();
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            if(feature.equals("relation")) {
                File relationfile = new File(RelationPath);
                BufferedReader relationreader = null;
                try {
                    relationreader = new BufferedReader(new FileReader(relationfile), 10 * 1024 * 1024);
                    String ss = null;
                    while((ss = relationreader.readLine()) != null) {
                        Relations r = new Relations();
                        r = getRelation(ss);
                        if(r == null) {
                            continue;
                        }
                        System.out.println(r.getId() + "," + r.getTag() + "\n" + relation2WKT(r, nodePath, wayPath, relationPath));
                        HandleFiles.WriteFile(wktfile, r.getId() + SplitStr + r.getTag() + SplitStr + relation2WKT(r, nodePath, wayPath, relationPath) + "\r\n");
                    }
                    relationreader.close();
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.out.println("writing wkt file failed: " + e.getLocalizedMessage());
            e.printStackTrace();
            return false;
        }
        System.out.println("writing wkt file done");
        return true;
    }

    private static void printUsage() {
        System.out.println("Usage\n"
                + "\t generate+cleanup from osm: >> osm2wkt mapfile.osm" + "\n"
                + "\t cleanup from wkt         : >> osm2wkt mapfile.wkt" + "\n"
                + "\t options: " + "\n"
                + "\t \t -o outputfile - write output to given file" + "\n"
                + "\t \t -a - append to output file" + "\n"
                + "\t \t -t X Y - translate map by x=X and y=Y meters" + "\n"
        );
    }

    public static String getEnName(String WikiID, String filePath) {
        String NameEn = "No English Name";
        File file = new File(filePath);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file), 10 * 1024 * 1024);
            String s = null;
            while ((s = reader.readLine()) != null) {
                String id = new String(s.substring(0, s.indexOf(",")));
                if(id.equals(WikiID)) {
                    NameEn = s.substring(s.indexOf(",") + 1);
                }
            }
            reader.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return NameEn;
    }

    public static void main(String[] args) {
		/*System.out.println("osm2wkt v1.2.0- convert " + "openstreetmap to wkt - Christoph P. Mayer - mayer@kit.edu");
		if(args.length < 1 || args.length > 7){
			printUsage();
			return;
		}*/

        OSM2WKT obj = new OSM2WKT();
        String node = "node";
        String way = "way";
        String relation = "relation";
        int translateX = 0;
        int translateY = 0;
        //先运行中国台湾的数据，再运行中国的数据，这里要注意对
        //NodePath = "F:\\NodePath.txt";
        //WayPath = "F:\\WayPath.txt";
        //RelationPath = "F:\\RelationPath.txt";
        //文档的保存、重命名
        //String file = "F:/taiwan-latest.osm";
        String rootPath = "F:\\SmallApple\\OSM-Wikidata_data\\Data\\OSM\\";
        String file = rootPath + "australia-latest.osm";
        //String file = "F:/OSMwithWiki_Taiwan.osm";
        //String file = "F:/china-latest.osm";
        //String file = "F:/OSMwithWiki_China.osm";
        String destfile1 = "F:\\OSM2WKT_Node.txt";
        String destfile2 = "F:\\OSM2WKT_Way.txt";
        String destfile3 = "F:\\OSM2WKT_Relation.txt";
        String Wiki_NameEn = "F:\\Wiki-Name_EN&&ID.csv";

        if (destfile1.length() == 0) destfile1 = file + "." + FILE_EXT_WKT;
        if (destfile2.length() == 0) destfile2 = file + "." + FILE_EXT_WKT;
        if (destfile3.length() == 0) destfile3 = file + "." + FILE_EXT_WKT;
        String filelower = file.toLowerCase();

        System.out.println("converting file " + file + " ...");
        obj.readOSM(file);
        /*if (filelower.endsWith(FILE_EXT_OSM)) {
            if (!obj.readOSM(file))
                return;
            //obj.readOSM(file);
            /*if (!obj.transformCoordinates())
                return;*/
            /*
            if (!obj.writeWkt(destfile1, node))
                return;

            if (!obj.writeWkt(destfile2, way))
                return;
            if (!obj.writeWkt(destfile3, relation))
                return;*/
        /*} else if (filelower.endsWith(FILE_EXT_WKT)) {
            if (!obj.readWkt(file))
                return;
            if (!obj.writeWkt(destfile1, node, NodePath, WayPath, RelationPath))
                return;
            if (!obj.writeWkt(destfile2, way, NodePath, WayPath, RelationPath))
                return;
            if (!obj.writeWkt(destfile3, relation, NodePath, WayPath, RelationPath))
                return;
        } else {
            System.out.println("unknown file extension in " + filelower);
            return;
        }*/
        //String s = way2WKT(getWaybyID("101917176", WayPath), NodePath);
        //System.out.println(s);
        //System.out.println("written to new file " + destfile1 + ", " + destfile2 + ", " + destfile3);
        System.out.println("done!");

    }
}


class Nodes {
    //对应于XML中的node，数据库中的Point
    private String id;
    private String lon;
    private String lat;
    private String tag;
    /*
    private String version;
    private String uid;
    private String user;
    private String changeset;
    private String timestamp;
    //private String visible;
    */

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!this.getClass().isInstance(o))
            return false;
        Nodes ol = (Nodes) o;
        return (this.id == ol.id);
    }

    public String getTag() {
        return tag;
    }
    public void setTag(String tag) {
        this.tag = tag;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getLon() {
        return lon;
    }
    public void setLon(String lon) {
        this.lon = lon;
    }
    public String getLat() {
        return lat;
    }
    public void setLat(String lat) {
        this.lat = lat;
    }
    /*
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }
    public String getChangeset() {
        return changeset;
    }
    public void setChangeset(String changeset) {
        this.changeset = changeset;
    }
    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    /*public String getVisible() {
        return visible;
    }
    public void setVisible(String visible) {
        this.visible = visible;
    }*/
}
class Way{
    //对应于XML中的way、数据库中的Polylin和Polygon
    private String id;
    private String tag;
    private List<Nodes> points;
    //private String pointids;
    private Vector<String> pointids;
    /*
    private String version;
    private String uid;
    private String user;
    private String changeset;
    private String timestamp;
    //private String visible;
    */

    /*public String getVisible() {
        return visible;
    }
    public void setVisible(String visible) {
        this.visible = visible;
    }*/
    public Vector<String> getPointids() {
        return pointids;
    }
    public void setPointids(Vector<String> pointids) {
        this.pointids = pointids;
    }
    public List<Nodes> getPoint() {
        return points;
    }
    public void setPoint(List<Nodes> points) {
        this.points = points;
    }
    public String getTag() {
        return tag;
    }
    public void setTag(String tag) {
        this.tag = tag;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    /*
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }
    public String getChangeset() {
        return changeset;
    }
    public void setChangeset(String changeset) {
        this.changeset = changeset;
    }
    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    */
}

class Relations {
    private String id;
    private String tag;
    private Vector<String> nodeIDs;
    private Vector<String> wayIDs;
    private Vector<String> relationIDs;
    /*
    private String version;
    private String uid;
    private String user;
    private String changeset;
    private String timestamp;
    //private String visible;
    */

    /*public String getVisible() {
        return visible;
    }
    public void setVisible(String visible) {
        this.visible = visible;
    }*/
    public Vector<String> getnodeIDs() {
        return nodeIDs;
    }
    public void setnodeIDs(Vector<String> nodeIDs) {
        this.nodeIDs = nodeIDs;
    }
    public Vector<String> getwayIDs() {
        return wayIDs;
    }
    public void setwayIDs(Vector<String> wayIDs) {
        this.wayIDs = wayIDs;
    }
    public Vector<String> getrelationIDs() {
        return relationIDs;
    }
    public void setrelationIDs(Vector<String> relationIDs) {
        this.relationIDs = relationIDs;
    }
    public String getTag() {
        return tag;
    }
    public void setTag(String tag) {
        this.tag = tag;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    /*
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }
    public String getChangeset() {
        return changeset;
    }
    public void setChangeset(String changeset) {
        this.changeset = changeset;
    }
    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    */
}
