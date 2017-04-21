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

public class OSMtoWKT extends DefaultHandler {

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
    private String kvcontentsW = "";
    private String kvcontentsN = "";
    //private String pointids = "";
    private Vector<String> pointids;
    private Vector<Nodes> points;
    private Nodes nodes;
    private Way way;
    private Relation relation;

    private List<Nodes> nodeslist;
    private List<Way> waylist;
    private List<Relation> relationlist;

    /**
     * 用streets和landmarks内存会溢出，需要另寻他法——先存到文件中保存起来，再进行匹配
     * 这一方法在OSM2WKT中实现
     */
    private HashMap<String, Vector<String>> streets;
    private HashMap<String, Nodes> landmarks;
    //private String NodePath = "F:\\NodePath.txt";
    //private String WayPath = "F:\\WayPath.txt";
    private String RelationPath = "F:\\Relation.txt";

    boolean Type = false, temTympe = false;
    private Integer countp = 0;
    private Integer countw = 0;
    //这俩值作为记录name的索引
    private Integer plagN = 0;
    private Integer plagW = 0;
    //private Integer plagN = 0; //用于只提取前四个标签
    //private Integer plagW = 0; //用于只提取前两个标签

    @Override
    public void startDocument() throws SAXException {
        nodeslist = new ArrayList<Nodes>();
        waylist = new ArrayList<Way>();
        relationlist = new ArrayList<Relation>();
        /**
         * 用streets和landmarks内存会溢出，需要另寻他法
         */
        landmarks = new HashMap<String, Nodes>();
        streets = new HashMap<String, Vector<String>>();
        System.out.println("正在读取XML(OSM)文档，如果数据量过大需要一段时间，请耐心等待……");
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        // 对node进行操作
        if ("node".equals(qName)) { //记录下node的id和经纬度
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
            //暂时不需要进行的操作，我们只需要记录下node的id和经纬度
            /*
            if (attributes.getValue("version") != null && attributes.getValue("version") != "")
                versioncontents = attributes.getValue("version");
            else
                versioncontents = "0";
            if (attributes.getValue("uid") != null && attributes.getValue("uid") != "")
                uidioncontents = attributes.getValue("uid");
            else
                uidioncontents = "0";
            if (attributes.getValue("user") != null && attributes.getValue("user") != "")
                usercontents = attributes.getValue("user");
            else
                usercontents = "0";
            if (attributes.getValue("changeset") != null && attributes.getValue("changeset") != "")
                changesetcontents = attributes.getValue("changeset");
            else
                changesetcontents = "0";
            if (attributes.getValue("timestamp") != null && attributes.getValue("timestamp") != "")
                timestampcontents = attributes.getValue("timestamp");
            else
                timestampcontents = "0";
            */
            // if(attributes.getValue("visible") != null && attributes.getValue("visible") != "") //不再存储visible字段
            // visiblecontents = attributes.getValue("visible");
            // else
            // visiblecontents = "0";
            curretntag = "node";
            countp++;
        }

        //if ("node".equals(curretntag) && "tag".equals(qName) && plagN < 4) {
        if ("node".equals(curretntag) && "tag".equals(qName) && plagN == 0) {
            String kcontents = attributes.getValue("k");
            String vcontents = attributes.getValue("v");
            if(!(kcontents.equals(XML_TAG_NAME))) { //提取出OSM实体way的name，没有的话为空
                kcontents = "";
                vcontents = "";
                kvcontentsN = "";
            }
            else {
                plagN = 1;
                //kvcontentsN = kcontents + "=" + vcontents;
                kvcontentsN = vcontents;
            }
            //plagW++;//用于只提取前四个标签
        }
        if("node".equals(curretntag) && "tag".equals(qName) && attributes.getValue("k").equals(XML_TAG_NAME + ":zh")) {
            String kcontents = attributes.getValue("k");
            String vcontents = attributes.getValue("v");
            //kvcontentsN = kcontents + "=" + vcontents;
            kvcontentsN = vcontents;
            plagN = 1;
        }

        //对way操作
        if ("way".equals(qName)) {
            pointids = new Vector<String>();
            points = new Vector<Nodes>();
            if (attributes.getValue("id") != null && attributes.getValue("id") != "")
                idcontents = attributes.getValue("id");
            else
                idcontents = "0";
            //暂时不需要进行的操作，我们只需要记录下way的id
            /*
            if (attributes.getValue("version") != null && attributes.getValue("version") != "")
                versioncontents = attributes.getValue("version");
            else
                versioncontents = "0";
            if (attributes.getValue("uid") != null && attributes.getValue("uid") != "")
                uidioncontents = attributes.getValue("uid");
            else
                uidioncontents = "0";
            if (attributes.getValue("user") != null && attributes.getValue("user") != "")
                usercontents = attributes.getValue("user");
            else
                usercontents = "0";
            if (attributes.getValue("changeset") != null && attributes.getValue("changeset") != "")
                changesetcontents = attributes.getValue("changeset");
            else
                changesetcontents = "0";
            if (attributes.getValue("timestamp") != null && attributes.getValue("timestamp") != "")
                timestampcontents = attributes.getValue("timestamp");
            else
                timestampcontents = "0";
            */
            // if(attributes.getValue("visible") != null && attributes.getValue("visible") != "") //不再存储visible字段
            // visiblecontents=attributes.getValue("visible");
            // else
            // visiblecontents = "0";
            curretntag = "way";
            countw++;
        }

        //if ("way".equals(curretntag) && "tag".equals(qName) && plagW < 2) {
        if ("way".equals(curretntag) && "tag".equals(qName) && plagW == 0) {
            String kcontents = attributes.getValue("k");
            String vcontents = attributes.getValue("v");
            if(!(kcontents.equals(XML_TAG_NAME))) { //提取出OSM实体way的name，没有的话为空
                kcontents = "";
                vcontents = "";
                kvcontentsW = "";
            }
            else {
                plagW = 1;
                //kvcontentsW = kcontents + "=" + vcontents;
                kvcontentsW = vcontents;
            }
            //plagW++;//用于只提取前两个标签
        }
        if("way".equals(curretntag) && "tag".equals(qName) && attributes.getValue("k").equals(XML_TAG_NAME + ":zh")) {
            String kcontents = attributes.getValue("k");
            String vcontents = attributes.getValue("v");
            //kvcontentsW = kcontents + "=" + vcontents;
            kvcontentsW = vcontents;
            plagW = 1;
        }

        if ("way".equals(curretntag) && "nd".equals(qName)) { //对way的引用node操作
            String ref = attributes.getValue("ref");
            pointids.add(ref);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        //对node进行处理
        if ("node".equals(qName)) {
            nodes = new Nodes();
            nodes.setId(idcontents);
            nodes.setLon(loncontents);
            nodes.setLat(latcontents);
            nodes.setTag(kvcontentsN);
            /*
            nodes.setVersion(versioncontents);
            nodes.setUid(uidioncontents);
            nodes.setUser(usercontents);
            nodes.setChangeset(changesetcontents);
            nodes.setTimestamp(timestampcontents);
            //nodes.setVisible(visiblecontents);
            */
            nodeslist.add(nodes);
            /**
             * 用landmarks内存会溢出，需要另寻他法
             */
            landmarks.put(nodes.getId(), nodes);

            //System.out.println(landmarks.size() + "\t" + nodeslist.size());
            //对要存满的nodelist进行处理
            if (nodeslist.size() >= 100000) {
                for (int i = 0; i < nodeslist.size(); i++) {
                    System.out.println("Node Id: " + nodeslist.get(i).getId() + "\tName: " + nodeslist.get(i).getTag());
                    //landmarks.put(nodeslist.get(i).getId(), nodeslist.get(i));
                }
                nodeslist.clear();
            }
            // 可以将节点的数据记录到文件里
            // HandleFiles.WriteFile(NodePATH,nodes);

            kvcontentsN = "";
            curretntag = "";
            plagN = 0;
            nodes = null;
        }

        //对way进行处理
        if ("way".equals(qName)) {
            //对前面没有处理完的nodeslist集合进行处理
            if (!nodeslist.isEmpty()) {
                for (int i = 0; i < nodeslist.size(); i++) {
                    System.out.println("Node Id: " + nodeslist.get(i).getId() + "\tName: " + nodeslist.get(i).getTag());
                    //landmarks.put(nodeslist.get(i).getId(), nodeslist.get(i));
                }
                nodeslist.clear();
            }
            way = new Way();
            nodes = new Nodes();
            way.setId(idcontents);
            way.setTag(kvcontentsW);
            way.setPointids(pointids);
            /*
            way.setVersion(versioncontents);
            way.setUid(uidioncontents);
            way.setUser(usercontents);
            way.setChangeset(changesetcontents);
            way.setTimestamp(timestampcontents);
            //way.setVisible(visiblecontents);
            */
            waylist.add(way);
            /**
             * 用streets内存会溢出，需要另寻他法
             */
            streets.put(way.getId(), way.getPointids());
            //对要存满的waylist进行处理
            if (waylist.size() >= 100000) {
                for (int i = 0; i < waylist.size(); i++) {
                    System.out.println("Way Id:" + waylist.get(i).getId() + "\tName: " + waylist.get(i).getTag());
                    //streets.put(waylist.get(i).getId(), waylist.get(i).getPointids());
                    try {
                        if (polygonOrPolyline(waylist.get(i).getPointids()))
                            Type = true;//polygon;
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
            // 可以将路径的数据记录到文件里
            // HandleFiles.WriteFile(WayPATH,way);
            kvcontentsW = "";
            curretntag = "";
            pointids = null;
            plagW = 0;
            way = null;
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

    private Long nextNodeIndex() {
        Long i = new Long(landmarks.size());
        for (; true; i++) {
            if (landmarks.containsKey(String.valueOf(i)) == false && i > 0) {
                //System.out.println("i=" + i);
                return i;
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
                    for (String s : landmarks.keySet()) {
                        Nodes n = landmarks.get(s);

                        //if(this.plainDistance(m.x, m.y, x, y) < 10){
                        if (Math.abs(Double.parseDouble(n.getLon()) - x) < epsilon && Math.abs(Double.parseDouble(n.getLat()) - y) < epsilon) {
                            currentmark = Long.parseLong(s);
                            break;
                        }
                    }
                    // need to generate new landmark
                    if (currentmark == -1) {
                        Nodes nd = new Nodes();
                        currentmark = nodeid++;
                        nd.setId(Long.toString(currentmark));
                        nd.setLon(Double.toString(x));
                        nd.setLat(Double.toString(y));
                        landmarks.put(nd.getId(), nd);
                        street.add(Long.toString(currentmark));
                    } else
                        street.add(Long.toString(currentmark));
                } //for(String item : parts)
                streets.put(String.valueOf(wayid++), street);
            }
        } catch (Exception e) {
            System.out.println("reading wkt file failed: " + e.getLocalizedMessage());
            e.printStackTrace();
            return false;
        }
        System.out.println("parsing wkt found " + streets.size() + " streets");
        return true;
    }

    private boolean fixCompleteness() {
        System.out.println("checking landmark completeness for all streets ...");
        for (Vector<String> s : streets.values()) {
            for (String mark : s) {
                if (!landmarks.containsKey(mark)) {
                    System.out.println("landmarks " + mark + " for street not found");
                    return false;
                }
            }
        }
        System.out.println("all required landmarks available for all streets");
        System.out.println("do you want to fix missing landmarks? " +
                "this will take very long but can heavily reduce map partitioning");
        System.out.print("type 'y' or 'n': ");
        int r = 0;
        try {
            r = System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (r != 'y') return true; // ?? on entering Y why are we returning shouldn't we start comparing
        System.out.println("checking for missing landmarks for crossing street parts. this may take a while ...");
        boolean changed = false;
        long missingLandmarks = 0;
        do {
            changed = false;
            // walk all streets
            for (String streetA : streets.keySet()) {
                Vector<String> streetPointsA = streets.get(streetA);
                int indexA = 0;
                long lastAP = -1;
                long currentAP = -1;
                Nodes lastAL = null;
                Nodes currentAL = null;
                // iterate over every street part of the current street
                for (Iterator<String> iterpA = streetPointsA.iterator(); iterpA.hasNext(); indexA++) {
                    String pA = iterpA.next();
                    if (lastAP == -1 || lastAL == null) {
                        lastAP = Long.parseLong(pA);
                        lastAL = landmarks.get(pA);
                        continue;
                    }
                    // street part goes from lastP to currentP
                    currentAP = Long.parseLong(pA);
                    currentAL = landmarks.get(pA);
                    // check to see if this street part crosses another street part
                    // this indicates a missing landmark at this position
                    // walk over possible crossing street
                    for (String streetB : streets.keySet()) {
                        Vector<String> streetPointsB = streets.get(streetB);
                        // don't check street with itself
                        if (streetA.equals(streetB))
                            continue;
                        // unchecked street parts, go...
                        int indexB = 0;
                        long lastBP = -1;
                        long currentBP = -1;
                        Nodes lastBL = null;
                        Nodes currentBL = null;

                        // walk over the street part of possible crossing street
                        for (Iterator<String> iterpB = streetPointsB.iterator(); iterpB.hasNext(); indexB++) {
                            String pB = iterpB.next();
                            //System.out.println("running " + indexA + " against index " + indexB);
                            if (lastBP == -1 || lastBL == null) {
                                lastBP = Long.parseLong(pB);
                                lastBL = landmarks.get(pB);
                                continue;
                            }
                            currentBP = Long.parseLong(pB);
                            currentBL = landmarks.get(pB);
                            // street part from lastBP to currentBP
                            // check for crossings in the two street parts
                            // [lastAP,currentAP] and [lastBP,currentBP]
                            Nodes crossing = checkCrossing(
                                    lastAL, currentAL,
                                    lastBL, currentBL
                            );
                            if (crossing != null) {
                                // add this id to a set
                                if (!streetPointsA.contains(crossing.getId())) {
                                    streetPointsA.add(indexA, crossing.getId());
                                    //System.out.println("Adding landmark to street A");
                                    changed = true;
                                }
                                if (!streetPointsB.contains(crossing.getId())) {
                                    streetPointsB.add(indexB, crossing.getId());
                                    //System.out.println("Adding landmark to street B");
                                    changed = true;
                                }
                                if (changed) {
                                    missingLandmarks++;
                                    fixCompletenessAddedLandmarks.add(crossing.getId());
                                    break;
                                }
                            } //if(crossing != null)
                            lastBP = currentBP;
                            lastBL = currentBL;
                        } //for(Iterator<Long> iterpB = streetPointsB.iterator(); iterpB.hasNext(); )
                        if (changed) break;
                    } //for(Long streetB : streets.keySet())
                    // move to next part
                    lastAP = currentAP;
                    lastAL = currentAL;
                    if (changed) break;
                } //for(Iterator<Long> iterpA = streetPointsA.iterator(); iterpA.hasNext(); )
                if (changed) break;
            } //for(Long streetA : streets.keySet())
        } while (changed);
        //print all the landmarks that were added
        //Iterator it = fixCompletenessAddedLandmarks.iterator();
        //while(it.hasNext())
        //{
        //	Long id = (Long) it.next();
        //	System.out.println(" : " + id);
        //}
        System.out.println("inserted " + missingLandmarks
                + " missing landmarks. currently have "
                + landmarks.size() + " landmarks");
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
        x = OSMtoWKT.round(x, OSMtoWKT.precisonFloating);
        //System.out.println("x = " + x);
        double y = (aA * bC - bA * aC) / det;
        y = OSMtoWKT.round(y, OSMtoWKT.precisonFloating);
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
        for (Nodes n : landmarks.values()) {
            if (Math.abs(Double.parseDouble(n.getLon()) - x) < epsilon && Math.abs(Double.parseDouble(n.getLat()) - y) < epsilon) {
                crossing = n;
                break;
            }
        }
        if (crossing == null) {
            crossing = new Nodes();
            crossing.setId(String.valueOf(nextNodeIndex()));
            crossing.setLon(String.valueOf(x));
            crossing.setLat(String.valueOf(y));
            landmarks.put(crossing.getId(), crossing);
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
        for (Nodes l : landmarks.values()) {
            if (Double.parseDouble(l.getLat()) < latMin)
                latMin = Double.parseDouble(l.getLat());
            if (Double.parseDouble(l.getLat()) > latMax)
                latMax = Double.parseDouble(l.getLat());
            if (Double.parseDouble(l.getLon()) < lonMin)
                lonMin = Double.parseDouble(l.getLon());
            if (Double.parseDouble(l.getLon()) > lonMax)
                lonMax = Double.parseDouble(l.getLon());
        }
        System.out.println("found geographic bounds:"
                + " latitude from " + latMin + " to " + latMax
                + " longitude from " + lonMin + " to " + lonMax);
        double width = Double.parseDouble(geoDistance(latMin, lonMin, latMin, lonMax));
        double height = Double.parseDouble(geoDistance(latMin, lonMin, latMax, lonMin));
        System.out.println("geographic area dimensions are: height " + height + "m, width " + width + "m");
        // put coordinate system to upper left corner with (0,0), output in meters
        for (Nodes l : landmarks.values()) {
            l.setLon(geoDistance(Double.parseDouble(l.getLat()), Double.parseDouble(l.getLon()), Double.parseDouble(l.getLat()), lonMin));
            l.setLat(geoDistance(Double.parseDouble(l.getLat()), Double.parseDouble(l.getLon()), latMin, Double.parseDouble(l.getLon())));
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
        distance = OSMtoWKT.round(distance, OSMtoWKT.precisonFloating);
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
        distance = OSMtoWKT.round(distance, OSMtoWKT.precisonFloating);
        return String.valueOf(distance);
    }

    private boolean translate(int x, int y) {
        if (x == 0 && y == 0) return true;
        System.out.println("translating map by x=" + x + " and y=" + y);
        for (Nodes mark : landmarks.values()) {
            mark.setLon( String.valueOf( Long.parseLong(mark.getLon()) + x ) );
            mark.setLat( String.valueOf( Long.parseLong(mark.getLat()) + x ) );
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
        // add all landmarks as vertexes
        for (String l : landmarks.keySet()) {
            Nodes nd = landmarks.get(l);
            assert (l.equals(nd.getId()));
            graph.addVertex(nd.getId());
        }
        // add all streets as edges between landmarks
        for (String s : streets.keySet()) {
            Vector<String> marks = streets.get(s);
            Nodes last = null;
            Nodes current = null;
            for (String m : marks) {
                current = landmarks.get(m);
                if (last == null) {
                    last = current;
                    continue;
                }
                assert (graph.containsVertex(last.getId()) && graph.containsVertex(current.getId()));
                graph.addEdge(last.getId(), current.getId());
                last = current;
            }
        } //for(Long s : streets.keySet())
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
                for (String lid : landmarks.keySet()) {
                    if (lid.equals(vertice)) {
                        landmarks.remove(lid);
                        removedL = true;
                        countRemovedLandmarks++;
                        break;
                    }
                }
            } while (removedL);
            boolean removedS;
            do {
                // remove all streets that contain this vertice
                removedS = false;
                for (String sid : streets.keySet()) {
                    Vector<String> street = streets.get(sid);
                    if (street.contains(vertice)) {
                        streets.remove(sid);
                        removedS = true;
                        countRemovedStreets++;
                        break;
                    }
                }
            } while (removedS);

        } //for(Long vertice : verticesRemove)
        System.out.println("removed " + countRemovedStreets
                + " unconnected streets and " + countRemovedLandmarks
                + " unconnected landmarks. know have "
                + streets.size() + " streets in map built upon "
                + landmarks.size() + " landmarks");
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
            for (Vector<String> s : streets.values()) {
                System.out.print(s + "\t" + WKT_TAG_BEGIN);
                wktstream.append(WKT_TAG_BEGIN);
                for (int i = 0; i < s.size(); i++) {
                    String l = s.elementAt(i);
                    Nodes mark = landmarks.get(l);
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
            wktstream.close();
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
        for (String l : landmarks.keySet()) {
            Nodes lm = landmarks.get(l);
            assert (l.equals(lm.getId()));
            weightedGraph.addVertex(lm.getId());
        }
        // add all streets as edges between landmarks
        for (String s : streets.keySet()) {
            Vector<String> marks = streets.get(s);
            Nodes last = null;
            Nodes current = null;
            for (String m : marks) {
                current = landmarks.get(m);
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
        } //for(Long s : streets.keySet())
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

        OSMtoWKT obj = new OSMtoWKT();
        boolean append = false;
        int translateX = 0;
        int translateY = 0;
        //String file = "F:\\OSMwithWiki_Taiwan.osm";
        String file = "F:/taiwan-latest.osm";
        String destfile = "F:\\OSM2WKT_Test.txt";
        obj.readOSM(file);
        //System.out.println("\ncountN: " + obj.countp + "\tcountW: " + obj.countw + "\t" + "Nodes: " + obj.landmarks.size() + "\tWays: " + obj.streets.size() + "\n");
        System.out.println("\ncountN: " + obj.countp + "\tcountW: " + obj.countw + "\n");

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


