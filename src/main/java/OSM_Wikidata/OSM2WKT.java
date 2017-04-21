package OSM_Wikidata;

/**
 * Created by SmallApple on 2017/4/18.
 */

import FileHandle.HandleFiles;
import exports.DOTExporter;
import exports.GraphMLExporter;
import exports.NAETOExporter;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.Pseudograph;
import org.jgrapht.graph.WeightedPseudograph;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.management.relation.Relation;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerConfigurationException;
import java.io.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
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
    private final static String WKT_TAG_BEGIN = "LINESTRING (";
    private final static String WKT_TAG_IBEGIN = "LINESTRING";
    private final static String WKT_TAG_BRACK1 = "(";
    private final static String WKT_TAG_BRACK2 = ")";
    private final static String WKT_TAG_END = ")";
    private final static String WKT_TAG_BREAK = "\n";
    private final static String WKT_TAG_MARKADD = " ";
    private final static String WKT_TAG_MARKSEP1 = ",";
    private final static String WKT_TAG_MARKSEP2 = " ";

    private WeightedPseudograph<String, DefaultWeightedEdge> weightedGraph;
    private WeightedPseudograph<String, DefaultWeightedEdge> testweightedGraph;
    private HashSet<String> fixCompletenessAddedLandmarks = new HashSet<String>();
    private HashSet<String> edgesAlreadyAdded = new HashSet<String>();
    static int precisonFloating = 3; // use 3 decimals after comma for rounding
    double epsilon = 0.0001;
    String Snum = new String();

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
    private String NodePath = "F:\\NodePath.txt";
    private String WayPath = "F:\\WayPath.txt";
    private String RelationPath = "F:\\RelationPath.txt";

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
            if(kcontents.equals(XML_TAG_NAME) || kcontents.equals(XML_TAG_NAME + ":zh")) { //提取出OSM实体way的name.如果有中文名，就记录下中文名
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

    private static Nodes getNode(String nodeLine) {
        if(nodeLine == null) {
            return null;
        }
        Nodes node = new Nodes();
        String[] nodeInf = nodeLine.split(SplitStr);
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
    private static Way getWay(String wayline) {
        if(wayline == null) {
            return null;
        }
        Way way = new Way();
        String[] wayInf = wayline.split(SplitStr);
        if(!isNumeric(wayInf[0])) {
            return null;
        }
        Vector<String> noderef = new Vector<String>();
        if(wayInf[2].length() > 2) {
            String nodeset = wayInf[2].substring(1, wayInf[2].length()-2);
            String[] nodes = nodeset.split(",");
            if(nodes.length <= 1) {
                noderef.add(nodeset.trim());
            } else {
                for (int i = 0; i < nodes.length; i++) {
                    noderef.add(nodes[i].trim());
                }
            }
        }
        way.setId(wayInf[0]);
        way.setPointids(noderef);
        way.setTag(wayInf[1]);
        //System.out.println(way.getId() + "," + way.getTag() + "," + way.getPointids());
        return way;
    }
    private static Nodes getNodebyID (String nodeid, String nodePath) {
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
                    System.out.println("Made");
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
        System.out.println(node.getId() + "," + node.getTag() + "," + node.getLon() + " " + node.getLat());
        return node;
    }

    private boolean polygonOrPolyline(Vector<String> nodes) {
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

    /*private Long nextNodeIndex() {
        Long i = new Long(landmarks.size());
        for (; true; i++) {
            if (landmarks.containsKey(String.valueOf(i)) == false && i > 0) {
                //System.out.println("i=" + i);
                return i;
            }
        }
    }*/
    private Long nextNodeIndex() {
        Long i = new Long(countp);
        for (; true; i++) {
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
                    String id = n.getId();
                    if(!id.equals(String.valueOf(i)) && i > 0) {
                        return i;
                    }
                }
                nodereader.close();
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static double round(double d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Double.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }

    private boolean readOSM(String filePath) {
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

    private boolean fixCompleteness() {
        System.out.println("checking landmark completeness for all streets ...");

        /**
         * 这里的工程量有点大，但是正常来说不会出现有节点找不到的情况，所以忽略
         */
        /*for (Vector<String> s : streets.values()) {
            for (String mark : s) {
                if (!landmarks.containsKey(mark)) {
                    System.out.println("landmarks " + mark + " for street not found");
                    return false;
                }
            }
        }*/
        System.out.println("all required landmarks available for all streets");
        return true;
    }

    private Nodes checkCrossing(Nodes a1, Nodes a2, Nodes b1, Nodes b2) {
        // see http://www.ucancode.net/faq/C-Line-Intersection-2D-drawing.htm
        // for 2d line crossing checks
        // line a --> aA*x+aB*y=aC
        double aA = Double.parseDouble(a2.getLat()) - Double.parseDouble(a1.getLat());
        double aB = Double.parseDouble(a1.getLon()) - Double.parseDouble(a2.getLon());
        double aC = aA * Double.parseDouble(a1.getLon())  + aB * Double.parseDouble(a1.getLat());
        // line b --> bA*x+bB*y=bC
        double bA = Double.parseDouble(b2.getLat()) - Double.parseDouble(b1.getLat());
        double bB = Double.parseDouble(b1.getLon()) - Double.parseDouble(b2.getLon());
        double bC = bA * Double.parseDouble(b1.getLon()) + bB * Double.parseDouble(b1.getLat());
        // crossing
        double det = aA * bB - bA * aB;
        if (det == 0) // lines are parallel
            return null;
        // set precision
        double x = (bB * aC - aB * bC) / det;
        x = OSM2WKT.round(x, OSM2WKT.precisonFloating);
        //System.out.println("x = " + x);
        double y = (aA * bC - bA * aC) / det;
        y = OSM2WKT.round(y, OSM2WKT.precisonFloating);
        //System.out.println("y = " + y);
        // check for x validity
        boolean valid = (Math.min( Double.parseDouble(a1.getLon()), Double.parseDouble(a2.getLon()) ) <= x) &&
                (x <= Math.max( Double.parseDouble(a1.getLon()), Double.parseDouble(a2.getLon()) )) &&
                (Math.min( Double.parseDouble(a2.getLat()), Double.parseDouble(a1.getLat()) ) <= y) &&
                (y <= Math.max( Double.parseDouble(a2.getLat()), Double.parseDouble(a1.getLat()) )) &&
                (Math.min( Double.parseDouble(b1.getLon()), Double.parseDouble(b2.getLon()) ) <= x) &&
                (x <= Math.max( Double.parseDouble(b1.getLon()), Double.parseDouble(b2.getLon()) )) &&
                (Math.min( Double.parseDouble(b2.getLat()), Double.parseDouble(b1.getLat()) ) <= y) &&
                (y <= Math.max( Double.parseDouble(b2.getLat()), Double.parseDouble(b1.getLat()) ));

        // crossing but not within the line dimensions
        if (!valid) return null;
        // valid crossing -> can we use existing landmark?
        Nodes crossing = null;
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
                if (Math.abs(Double.parseDouble(n.getLon()) - x) < epsilon && Math.abs(Double.parseDouble(n.getLat()) - y) < epsilon) {
                    crossing = n;
                    break;
                }
            }
            nodereader.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (crossing == null) {
            crossing = new Nodes();
            crossing.setId(String.valueOf(nextNodeIndex()));
            crossing.setLon(String.valueOf(x));
            crossing.setLat(String.valueOf(y));
            crossing.setTag("New Generated Node");
            HandleFiles.WriteFile(NodePath, crossing.getId() + SplitStr + crossing.getLon() + SplitStr + crossing.getLat() + SplitStr + crossing.getTag() + "\r\n");
            countp++;
        }
        return crossing;
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

    private boolean simplifyModel(boolean repair) {
        for (int i = 1; i <= 10; i++) {
            System.out.println("simplicatiation run " + i + " of max 10");
            boolean connected = simplifyGraph(repair);
            if(connected)
                break;
        }
        return true;
    }

    private boolean simplifyGraph(boolean repair) {
        System.out.println("simplyfing model, removing unconnected parts ...");
        // create a graph using JGraphT
        Pseudograph<String, DefaultEdge> graph = new Pseudograph<String, DefaultEdge>(DefaultEdge.class);
        // add all nodes as vertexes
        File nodefile = new File(NodePath);
        BufferedReader nodereader = null;
        try {
            nodereader = new BufferedReader(new FileReader(nodefile), 10 * 1024 * 1024);
            String s = null;
            while ((s = nodereader.readLine()) != null) {
                Nodes nd = new Nodes();
                nd = getNode(s);
                s = null;
                if(nd == null) {
                    continue;
                }
                String l = nd.getId();
                assert (l.equals(nd.getId()));
                graph.addVertex(nd.getId());
            }
            nodereader.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        // add all ways as edges between nodes
        File wayfile = new File(WayPath);
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
                Vector<String> marks = w.getPointids();
                String s = w.getId();
                Nodes last = new Nodes();
                Nodes current = new Nodes();
                for(int i=0; i<marks.size(); i++) {
                    String m = marks.elementAt(i);
                    System.out.println(m);
                    current = getNodebyID(m, NodePath);
                    if (last == null) {
                        last = current;
                        continue;
                    }
                    assert (graph.containsVertex(last.getId()) && graph.containsVertex(current.getId()));
                    System.out.println(last.getId() + "," + current.getId() + "," + marks.size() + "," + m);
                    graph.addEdge(last.getId(), current.getId());
                    last = current;
                }
            }
            wayreader.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        // check graph for connectivity, are there unconnected partitions?
        ConnectivityInspector<String, DefaultEdge>
                inspector = new ConnectivityInspector<String, DefaultEdge>(graph);
        if (inspector.isGraphConnected()) {
            System.out.println("graph is connected, nothing to simplify");
            return true;
        }
        // we have partitions :(
        System.out.println("graph is not connected, analyzing partitions ...");
        List<Set<String>> partitions = inspector.connectedSets();
        System.out.print("found " + partitions.size() + " partitions: ");
        // print the different partition sizes
        long count = 0;
        for (Set<String> partition : partitions) {
            System.out.print("[" + partition.size() + "] ");
            count += partition.size();
        }
        System.out.print("\n");
        // search for the largest partition, this one will be used
        Set<String> largestpartition = null;
        for (Set<String> partition : partitions) {
            if (largestpartition == null)
                largestpartition = partition;
            else if (partition.size() > largestpartition.size())
                largestpartition = partition;
        }
        if(largestpartition != null) {
            System.out.println("selecting largest partition of landmark size " + largestpartition.size()
                    + ". removing unselected partitions of estimated " + Math.abs(count - largestpartition.size())
                    + " landmarks");
        }
        if (!repair) {
            System.out.println("not reparing");
            return true;
        }
        // have we encountered cases in which the graph came out to be unconnected
        // collect all vertices that are in the other than largest partitions
        HashSet<String> verticesRemove = new HashSet<String>();
        for (Set<String> partition : partitions) {
            if (partition.size() == largestpartition.size()) continue;
            verticesRemove.addAll(partition);
        }
        System.out.println("analyzed " + verticesRemove.size() + " landmarks to remove");
        // remove vertices and edges from unused partitions
        int countRemovedLandmarks = 0;
        int countRemovedStreets = 0;
        for (String vertice : verticesRemove) {
            boolean removedL;
            do {
                // remove all landmark
                removedL = false;
                File nodefile1 = new File(NodePath);
                BufferedReader nodereader1 = null;
                try {
                    nodereader1 = new BufferedReader(new FileReader(nodefile1), 10 * 1024 * 1024);
                    String s = null;
                    while ((s = nodereader1.readLine()) != null) {
                        Nodes n = new Nodes();
                        n = getNode(s);
                        if(n == null) {
                            continue;
                        }
                        String lid = n.getId();
                        if (lid.equals(vertice)) {
                            //landmarks.remove(lid);
                            /**
                             * 要删除这个ID为lid的节点
                             */
                            removedL = true;
                            countRemovedLandmarks++;
                            break;
                        }
                    }
                    nodereader.close();
                } catch (FileNotFoundException e) {
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } while (removedL);
            boolean removedS;
            do {
                // remove all streets that contain this vertice
                removedS = false;
                File wayfile1 = new File(WayPath);
                BufferedReader wayreader1 = null;
                try {
                    wayreader1 = new BufferedReader(new FileReader(wayfile1), 10 * 1024 * 1024);
                    String s = null;
                    while ((s = wayreader1.readLine()) != null) {
                        Way w = new Way();
                        w = getWay(s);
                        if(w == null) {
                            continue;
                        }
                        String sid = w.getId();
                        Vector<String> street = w.getPointids();
                        if (street.contains(vertice)) {
                            //streets.remove(sid);
                            /**
                             * 要删除这个ID为sid的路径
                             */
                            removedS = true;
                            countRemovedStreets++;
                            break;
                        }
                    }
                    wayreader.close();
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } while (removedS);

        } //for(Long vertice : verticesRemove)
        System.out.println("removed " + countRemovedStreets
                + " unconnected streets and " + countRemovedLandmarks
                + " unconnected landmarks. know have "
                + countw + " streets in map built upon "
                + countp + " landmarks");
        return false;
    }

    private boolean writeWkt(String wktfile, boolean append) {
        System.out.println("writing wkt file ...");
        try {
            File wkt = new File(wktfile);
            if (!append) {
                if (wkt.exists()) wkt.delete();
                wkt.createNewFile();
            }
            FileWriter wktstream = new FileWriter(wkt, append);
            if (append) {
                wktstream.append("\n");
                wktstream.append("\n");
                wktstream.append("\n");
            }
            File wayfile = new File(WayPath);
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
                    Vector<String> s = w.getPointids();
                    System.out.print(s + "\t" + WKT_TAG_BEGIN);
                    wktstream.append(WKT_TAG_BEGIN);
                    for (int i = 0; i < s.size(); i++) {
                        String l = s.elementAt(i);
                        Nodes mark = getNodebyID(l, NodePath);
                        System.out.print(mark.getLon() + WKT_TAG_MARKADD + mark.getLat());
                        wktstream.append(mark.getLon() + WKT_TAG_MARKADD + mark.getLat());
                        if (i + 1 < s.size()) {
                            wktstream.append(WKT_TAG_MARKSEP1 + WKT_TAG_MARKSEP2);
                            System.out.print(WKT_TAG_MARKSEP1 + WKT_TAG_MARKSEP2);
                        }
                    }
                    wktstream.append(WKT_TAG_END + WKT_TAG_BREAK);
                    System.out.println(WKT_TAG_END + WKT_TAG_BREAK);
                }
                wayreader.close();
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            System.out.println("writing wkt file failed: " + e.getLocalizedMessage());
            e.printStackTrace();
            return false;
        }
        System.out.println("writing wkt file done");
        return true;
    }

    private boolean PreparingWeightedGraph() {
        System.out.println("Preparing weighted graph called");
        // create a graph using JGraphT
        weightedGraph = new WeightedPseudograph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        int numEdgesAdded = 0;
        // add all landmarks as verteces
        File nodefile = new File(NodePath);
        BufferedReader nodereader = null;
        try {
            nodereader = new BufferedReader(new FileReader(nodefile), 10 * 1024 * 1024);
            String s = null;
            while ((s = nodereader.readLine()) != null) {
                Nodes lm = new Nodes();
                lm = getNode(s);
                if(lm == null) {
                    continue;
                }
                String l = lm.getId();
                assert (l.equals(lm.getId()));
                weightedGraph.addVertex(lm.getId());
            }
            nodereader.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }

        // add all streets as edges between landmarks
        File wayfile = new File(WayPath);
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
                String s = w.getId();
                Vector<String> marks = w.getPointids();
                Nodes last = null;
                Nodes current = null;
                for (int i = 0; i < marks.size(); i++) {
                    String m = marks.elementAt(i);
                    current = getNodebyID(m, NodePath);
                    if (last == null) {
                        last = current;
                        continue;
                    }
                    assert (weightedGraph.containsVertex(last.getId()) &&
                            weightedGraph.containsVertex(current.getId()));
                    // I hope adding edges more than once will not create any problem
                    // Yes it creates a lot of trouble ...
                    // 1) the pseudograph implementation screws up the weight parameter
                    // 2) this is the not the graph we that we have obtained from osm data
                    //    osm data only gives you one edge between two points
                    //    For osm we have the specification of two joints which
                    //    form the end points of the edges and no specification
                    //    of the edge between the two vertices is provided. So when
                    //    we say we have and edge between two points it the the same
                    //    edge how many ever times we say that and in what order we say that
                    String edge1 = last.getId() + " " + current.getId();
                    String edge2 = current.getId() + " " + last.getId();
                    double weight;
                    if (!edgesAlreadyAdded.contains(edge1) && !edgesAlreadyAdded.contains(edge2)) {
                        weightedGraph.addEdge(last.getId(), current.getId());
                        numEdgesAdded += 1;
                        // following two vertices are already connected by an edge
                        edge1 = last.getId() + " " + current.getId();
                        edgesAlreadyAdded.add(edge1);
                        edge2 = current.getId() + " " + last.getId();
                        edgesAlreadyAdded.add(edge2);

                        DefaultWeightedEdge weightedEdge = weightedGraph.getEdge(last.getId(), current.getId());
                        weight = plainDistance(last, current);
                        weightedGraph.setEdgeWeight(weightedEdge, weight);
                        last = current;
                    } else {
                        System.out.println("the edge has already been added to the graph");
                    }
                    last = current;
                }
            }
            wayreader.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return true;  // everything was completed successfully
    }


    //// dot format
    private boolean exportDOT(String destfile) {
        destfile = destfile + "." + "SNA_DOT.dat";
        // first open a File
        // the associate a FileWriter to it
        // keep appending data to Filewrtier stream
        // then close the FileWriter
        // I do not know why but probably you do not need to close the file
        File file = new File(destfile);
        try {
            FileWriter dotStream = new FileWriter(file, false); // false as no appending is to be done
            // i have make weightedgraph available to this function
            DOTExporter<String, DefaultWeightedEdge> dotexport = new DOTExporter<String, DefaultWeightedEdge>();
            // create a new writer with a new file name
            dotexport.export(dotStream, weightedGraph);
        } catch (java.io.IOException error) {
            // whatever
        }
        System.out.println("graph exported in dot format to " + destfile);
        return true;
    }

    //// naeto format
    private boolean exportNAETO(String destfile) {
        destfile = destfile + "." + "SNA_NAETO.dat";
        // first open a File
        // the associate a FileWriter to it
        // keep appending data to Filewrtier stream
        // then close the FileWriter
        // I do not know why but probably you do not need to close the file
        File file = new File(destfile);
        try {
            FileWriter naetoStream = new FileWriter(file, false); // false as no appending is to be done
            // i have make weightedgraph available to this function
            NAETOExporter<String, DefaultWeightedEdge> naetoexport = new NAETOExporter<String, DefaultWeightedEdge>();
            // create a new writer with a new file name
            naetoexport.export(naetoStream, weightedGraph);
        } catch (java.io.IOException error) {
            // whatever
        }
        System.out.println("graph exported in naeto format to " + destfile);
        return true;
    }


    //GraphML
    private boolean exportGraphML(String destfile) {
        destfile = destfile + "." + "SNA_GraphML.dat";
        // first open a File
        // the associate a FileWriter to it
        // keep appending data to Filewrtier stream
        // then close the FileWriter
        // I do not know why but probably you do not need to close the file
        File file = new File(destfile);
        try {
            FileWriter graphmlStream = new FileWriter(file, false); // false as no appending is to be done
            // i have make weightedgraph available to this function
            GraphMLExporter<String, DefaultWeightedEdge> graphmlexport = new GraphMLExporter<String, DefaultWeightedEdge>();
            // create a new writer with a new file name
            graphmlexport.export(graphmlStream, weightedGraph);
        } catch (java.io.IOException error) {
            // whatever
        } catch (TransformerConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("graph exported in GraphML format to " + destfile);
        return true;
    }


    // this function is to remove edges with very small weights
    // they are a result of edges being added more than once or
    // edges adding due to floating point comparision error
    // while fixCompleteness
    private boolean removeBogusEdges() {
        System.out.println(" ");
        // ConcurrentModificationException occurs because i have a iterator iterating through
        // the edgeSet and i'm modifing it at the same time
        // this is unacceptable
        // So make a list of these edge while iterating and then remove them from the graph
        ArrayList<DefaultWeightedEdge> removeEdge = new ArrayList<DefaultWeightedEdge>();
        for (DefaultWeightedEdge e : weightedGraph.edgeSet()) {
            double weight = weightedGraph.getEdgeWeight(e);
            if (Math.abs(weight - 1.0) < 0.0001 || weight <= 1.0E-09) {
                System.out.println("Edge with small weight : " + weight);
                removeEdge.add(e);
            }
            //remove edges in the List
        }
        int size = weightedGraph.edgeSet().size();
        System.out.println("size of the graph before removing edges : " + size);
        size = removeEdge.size();
        System.out.println("total number of unnecessary edges : " + size);
        for (DefaultWeightedEdge edge : removeEdge) {
            // checking whether edge has been successfully removed
            weightedGraph.removeEdge(edge);
            if (!weightedGraph.containsEdge(edge))
                System.out.println("edge successfully removed");
            else
                System.out.println("failure in removing the edge ");
        }
        size = weightedGraph.edgeSet().size();
        System.out.println("size of the graph after removing edges : " + size);
        // after the removal the graph may have some partitions so try and get the biggest partitions of these
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

    public static void main(String[] args) {
		/*System.out.println("osm2wkt v1.2.0- convert " + "openstreetmap to wkt - Christoph P. Mayer - mayer@kit.edu");
		if(args.length < 1 || args.length > 7){
			printUsage();
			return;
		}*/

        OSM2WKT obj = new OSM2WKT();
        boolean append = false;
        int translateX = 0;
        int translateY = 0;
        //String file = "F:\\OSMwithWiki_Taiwan.osm";
        String file = "F:/taiwan-latest.osm";
        String destfile = "F:\\OSM2WKT_Test.txt";
        //obj.readOSM(file);
        //System.out.println("\ncountN: " + obj.countp + "\tcountW: " + obj.countw + "\n");

        if (destfile.length() == 0) destfile = file + "." + FILE_EXT_WKT;
        String filelower = file.toLowerCase();

        System.out.println("converting file " + file + " ...");
        if (filelower.endsWith(FILE_EXT_OSM)) {
            /*if (!obj.readOSM(file))
                return;
            System.out.println("\nNodes: " + xml.landmarks.size() + "\tWays: " + xml.streets.size() + "\n");*/
            if (!obj.transformCoordinates())
                return;
            // this is where( simplifyGraph ) the graph is constructed
            // so you can create a weighted graph here and see if we get weird edges
            // that is edges with very small weights or edges with weight of 1
            // also calculate the number of nodes and edges in the graph at this point

            if (!obj.fixCompleteness())
                return;
            if (!obj.simplifyModel(true))
                return;
            if (!obj.translate(translateX, translateY))
                return;
            /** just after this function gets completed i think the graph
             is complete and we can make a pseudo graph out of it and
             export to a a format which is acceptable by a SNA software
             also one can input this graph to SNA libraries existing in java itself
             **/
            if (!obj.PreparingWeightedGraph())
                return;
            if (!obj.removeBogusEdges())
                return;
            if (!obj.simplifyModel(true))
                return;
            //if(!obj.exportDOT(destfile)) 	return;
            //if(!obj.exportNAETO(destfile)) 	return;
            //if(!obj.exportGml(destfile)) 	return;
            //if(!obj.exportGraphML(destfile)) 	return;
            //if(!obj.exportMatrix(destfile)) 	return;
            if (!obj.writeWkt(destfile, append))
                return;
        } else if (filelower.endsWith(FILE_EXT_WKT)) {
            if (!obj.readWkt(file))
                return;
            /** try and prepare a weighted graph right now
             and see if there are any bogus edges
             then clear the graph removeAllEdges removeAllVertices
             and later whenver you want reconstruct the graph
             **/
            if (!obj.fixCompleteness())
                return;
            if (!obj.simplifyModel(true))
                return;
            if (!obj.translate(translateX, translateY))
                return;
            if (!obj.PreparingWeightedGraph())
                return;
            if (!obj.removeBogusEdges())
                return;
            if (!obj.fixCompleteness())
                return;
            if (!obj.simplifyModel(true))
                return;
            /* exporting the graph
             if(!obj.exportDOT(destfile)) 	return;
             if(!obj.exportNAETO(destfile)) 	return;
             if(!obj.exportGraphML(destfile)) 	return;
             if(!obj.exportGml(destfile)) 	return;
             if(!obj.exportMatrix(destfile)) 	return;
             done exporting now writing files
             */
            if (!obj.writeWkt(destfile, append))
                return;
        } else {
            System.out.println("unknown file extension in " + filelower);
            return;
        }
        System.out.println("written to new file " + destfile);
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
