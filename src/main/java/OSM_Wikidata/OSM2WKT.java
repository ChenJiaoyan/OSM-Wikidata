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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import OSM.*;

public class OSM2WKT extends DefaultHandler {

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

    private Vector<String> pointids;
    private Vector<Nodes> points;
    private Vector<String> nodeIDs;
    private Vector<String> wayIDs;
    private Vector<String> relationIDs;
    private Nodes nodes;
    private Way way;
    private Relation relation;

    private List<Nodes> nodeslist;
    private List<Way> waylist;
    private List<Relation> relationlist;

    /**
     * 用HashMap进行存储内存会溢出，这是OSMtoWKT使用的方法
     * 需要另寻他法——先存到文件中保存起来，再进行匹配
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

    private static String SplitStr = "--";
    //nodeItemNum表示的是nodePath(wiki)里的node字段数目
    private static Integer nodeItemNum = 7;
    //wayItemNum表示的是wayPath(Wiki)里的way字段数目
    private static Integer wayItemNum = 6;
    //relationItemNum表示的是relationPath(Wiki)里的relation字段数目
    private static Integer relationItemNum = 8;

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
        // "NodePath_Area(Wiki).txt";      格式：nodeID--链接的wikidata ID--Name--Name_en--Name_zh--nodeLon--nodeLat
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
        if(!isNumeric(nodeInf[0]) || nodeInf.length < nodeItemNum) {
            return null;
        }
        node.setId(nodeInf[0]);
        node.setTag(nodeInf[1]);
        node.setLabel(nodeInf[2]);
        node.setName_en(nodeInf[3]);
        node.setName_zh(nodeInf[4]);
        node.setLon(nodeInf[5]);
        node.setLat(nodeInf[6]);
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
                if(nodeInf.length < 3) continue;
                String id = nodeInf[0];
                if(nodeid.equals(id)) {
                    node.setId(id);
                    node.setLon(nodeInf[1]);
                    node.setLat(nodeInf[2]);
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
        // "WayPath_Area(Wiki).txt";       格式：wayID--链接的wikidata ID--Name--Name_zh--Name_en--引用的node ID集合
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
        if(wayInf[wayItemNum-1].length() > 2) {
            String nodeset = wayInf[wayItemNum-1].substring(1, wayInf[wayItemNum-1].length() - 1);
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
        way.setTag(wayInf[1]);
        way.setLabel(wayInf[2]);
        way.setName_en(wayInf[3]);
        way.setName_zh(wayInf[4]);
        way.setPointids(noderef);

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
                if(wayInf.length < 2) continue;
                String id = wayInf[0];
                if(wayid.equals(id)) {
                    Vector<String> noderef = new Vector<>();
                    if(wayInf[1].length() > 2) {
                        String nodeset = wayInf[1].substring(1, wayInf[1].length()-1);
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

    public static Relation getRelation(String relationline) {
        // "RelationPath_Area(Wiki).txt";  格式：relationID--链接的wikidata ID--Name--Name_en--Name_zh--引用的node ID集合--引用的way ID集合--引用的relation ID集合
        if(relationline == null) {
            return null;
        }
        Relation relation = new Relation();
        //String[] relationInf = relationline.split(SplitStr);
        String[] relationInf = getSplit(relationline);
        if(!isNumeric(relationInf[0])) {
            return null;
        }
        Vector<String> noderef = new Vector<String>();
        Vector<String> wayref = new Vector<String>();
        Vector<String> relationref = new Vector<String>();
        if(relationInf[relationItemNum-3].length() > 2) {
            String nodeset = relationInf[relationItemNum-3].substring(1, relationInf[relationItemNum-3].length()-1);
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
        if(relationInf[relationItemNum-2].length() > 2) {
            String wayset = relationInf[relationItemNum-2].substring(1, relationInf[relationItemNum-2].length()-1);
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
        if(relationInf[relationItemNum-1].length() > 2) {
            String relationset = relationInf[relationItemNum-1].substring(1, relationInf[relationItemNum-1].length()-1);
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
        relation.setLabel(relationInf[2]);
        relation.setName_en(relationInf[3]);
        relation.setName_zh(relationInf[4]);
        relation.setnodeIDs(noderef);
        relation.setwayIDs(wayref);
        relation.setrelationIDs(relationref);
        return relation;
    }

    public static Relation getRelationByID(String relationID, String relationPath) {
        if(relationID == null || !isNumeric(relationID)) {
            return null;
        }
        Relation relation = new Relation();
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
                if(relationInf.length < 4) continue;
                String id = relationInf[0];
                if(relationID.equals(id)) {
                    if(relationInf[1].length() > 2) {
                        String nodeset = relationInf[1].substring(1, relationInf[1].length()-1);
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
                    if(relationInf[2].length() > 2) {
                        String wayset = relationInf[2].substring(1, relationInf[2].length()-1);
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
                    if(relationInf[3].length() > 2) {
                        String relationset = relationInf[3].substring(1, relationInf[3].length()-1);
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
    private boolean readWkt(String filePath, String nodePath, String WayPath) {
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
                    File nodefile = new File(nodePath);
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
                        HandleFiles.WriteFile(nodePath, nd.getId() + SplitStr + nd.getLon() + SplitStr + nd.getLat() + SplitStr + nd.getTag() + "\r\n");
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

    private boolean transformCoordinates(String nodePath) {
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
        File nodefile = new File(nodePath);
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
        File nodefile1 = new File(nodePath);
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

    private boolean translate(int x, int y, String nodePath) {
        if (x == 0 && y == 0) return true;
        System.out.println("translating map by x=" + x + " and y=" + y);
        File nodefile = new File(nodePath);
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

    public static String relation2WKT(Relation relation, String nodePath, String wayPath, String relationPath) {
        System.out.println("Before:" + relation.getnodeIDs() + "," + relation.getwayIDs() + "," + relation.getrelationIDs());
        String str = "";
        Vector<String> noder = new Vector<>();
        Vector<String> wayr = new Vector<>();
        if(relation.getnodeIDs() != null ) {
            noder = relation.getnodeIDs();
        }
        if(relation.getwayIDs() != null) {
            wayr = relation.getwayIDs();
        }
        if(relation.getrelationIDs() != null) {
            for (int i = 0; i < relation.getrelationIDs().size(); i++) {
                String sr = relation.getrelationIDs().elementAt(i);
                /**
                 * 很神奇，在osm文件里relation的relation reference中，找不到对应的node id或way id
                 * 比如说，relation,270056,中国,Q148，有<member type="relation" ref="913011" role="subarea"/>这条reference，
                 * 但是在osm文件里搜索不到id为913011的relation、
                 */
                Relation re = getRelationByID(sr, relationPath);
                if (re != null && re.getnodeIDs() != null) {
                    for (int j = 0; j < re.getnodeIDs().size(); j++) {
                        noder.add(re.getnodeIDs().get(j));
                        //System.out.println(re.getnodeIDs().get(j));
                    }
                }
                if (re != null && re.getwayIDs() != null) {
                    for (int k = 0; k < re.getwayIDs().size(); k++) {
                        wayr.add(re.getwayIDs().get(k));
                        //System.out.println(re.getwayIDs().get(k));
                    }
                }
            }
            getNoSameObjectVector(noder);
            getNoSameObjectVector(wayr);
            relation.setnodeIDs(noder);
            relation.setwayIDs(wayr);
//            noder.clear();
//            wayr.clear();
        }
        // 如果经过处理的relation只有node的reference
        System.out.println("After:" + relation.getnodeIDs() + "," + relation.getwayIDs() + "," + relation.getrelationIDs());
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
                    way = getWaybyID(relation.getwayIDs().elementAt(i), wayPath);
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

    private static Vector getNoSameObjectVector(Vector vector){
        Vector tempVector = new Vector();
        HashSet set = new HashSet(vector);
        //addAll(Collection c);  //可以接受Set和List类型的参数
        tempVector.addAll(set);
        return tempVector;

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
                File relationfile = new File(relationPath);
                BufferedReader relationreader = null;
                try {
                    relationreader = new BufferedReader(new FileReader(relationfile), 10 * 1024 * 1024);
                    String ss = null;
                    while((ss = relationreader.readLine()) != null) {
                        Relation r = new Relation();
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

        OSM2WKT obj = new OSM2WKT();
        String node = "node";
        String way = "way";
        String relation = "relation";
        /*
        int translateX = 0;
        int translateY = 0;

        String destfile1 = "OSM2WKT_Node.txt";
        String destfile2 = "OSM2WKT_Way.txt";
        String destfile3 = "OSM2WKT_Relation.txt";
        */
        //文档的保存、重命名
        //String file = "taiwan-latest.osm";
        //String file = "china-latest.osm";
        //String file = "OSMwithWiki_Taiwan.osm";
        //String file = "OSMwithWiki_China.osm";
        String rootPath = "F:\\SmallApple\\OSM-Wikidata_data\\Data\\OSM\\";
        String rootPath2 = "F:\\SmallApple\\OSM-Wikidata_data\\other\\";
        String file = rootPath + "australia-latest.osm";
        /**
         * Test 测试一下几个WKT格式转换函数是否正确
         */
        /*
        Nodes n = getNodebyID("8530018", rootPath2 + "OSMNode_Australia.txt");
        System.out.println(n.getId() + "\t" + n.getLon() + "\t" + n.getLat());
        System.out.println(node2WKT(n));
        */
        /*
        Way w = getWaybyID("3188360", rootPath2 + "OSMWay_Australia.txt");
        System.out.println(w.getId() + "\t" + w.getPointids());
        System.out.println(way2WKT(w, rootPath2 + "OSMNode_Australia.txt"));
        */


        // 172053只有way的引用 172053--Q5251198--R4------[]--[37634251, 37634250]--[]
        // 9057只有relation的引用 9057--Q6811970--Tram 75------[]--[]--[6170462, 6170461, 6361081]
        // After:[4267860392, 4267860389, 4267858587, 4267858585, 4267858583, 4267858581, 4267858579, 4267858578, 4267858575, 2260151181, 4267858573, 4267858572, 4266292180, 4266292179, 4266292177, 1453758546, 2256253962, 4266292173, 4266292171, 272268719, 4266292170, 2254767162, 4266292168, 2254765685, 4266292165, 2254759816, 4266292163, 2254759819, 4266292161, 2254759793, 4264566381, 2254727624, 4264566379, 2254467886, 4264566376, 2254705079, 4264566374, 2254698801, 4264566371, 2254691954, 4264566370, 2254683065, 4264566367, 2254680005, 4264566366, 2254674648, 4231826464, 2254667655, 4231826458, 2254663239, 4231826454, 2253533857, 4133111073, 2253470303, 4214707549, 2253531879, 4214707547, 2253531883, 4214707545, 2253526057, 4214707543, 2251576023, 4214707541, 2251573369, 4214707539, 2251570724, 4214707537, 2251556950, 4000703815, 2251293897, 4214707535, 2251292224, 4214707533, 4169720275, 2250248628, 4169720272, 2250222952, 4169720269, 2250245483, 4169720268, 2250239916, 4169720265, 4157116142, 2243307691, 4157116139, 2243314037, 1459448707, 2244662258, 4157116138, 2244375211, 4157116135, 2244367959, 4157116133, 2244681699, 2244683130, 2244684870, 1449316620, 1449308531, 1449308533, 4148845977, 2244704580, 4148845976, 2244706615, 3945798995, 3945798991, 3945798977, 591430826, 3945798965, 3945798958, 1479897588, 3945796051, 3947038101, 3947038099, 3947038097, 3947038094, 3937113698, 3937113699, 3947038095, 3947038096, 3947038098, 3947038100, 589527617, 3945796056, 3945798957, 3945798964, 3925153648, 3945798976, 3945798990, 2244706617, 3945798994, 2244704581, 4148845975, 4148845978, 1449308639, 1449308535, 2244684867, 1449316592, 2244683131, 2244681698, 1463620486, 4157116132, 2244375213, 4157116134, 2244662253, 4157116137, 2243314039, 1459448721, 2243307696, 4157116140, 4157116141, 2250239912, 4169720264, 2250245484, 4169720267, 2250223008, 4169720270, 2250248627, 4169720271, 4169720276, 2251292132, 4214707532, 2251293898, 4214707534, 2251285283, 4000703814, 2251570721, 4214707536, 2251573376, 4214707538, 2251576021, 4214707540, 2251425026, 4214707542, 2253531880, 4214707544, 2253531881, 4214707546, 2253461840, 4214707548, 2253533861, 4133111071, 2253541391, 4231826452, 2254663242, 4231826453, 2254667648, 4231826457, 2254674645, 4231826463, 2254679981, 4264566365, 2254683066, 4264566368, 2254691951, 4264566369, 2254698804, 4264566372, 2254705080, 4264566373, 2254710403, 4264566375, 2254467910, 4264566377, 2254727619, 4264566378, 2254759812, 4264566380, 2254759802, 4266292162, 2254759807, 4266292164, 2254765683, 4266292166, 2254767160, 4266292167, 2256243638, 4266292169, 4266292172, 2256253961, 4266292174, 4266292175, 4266292176, 4266292178, 4266292181, 4267858571, 2260151180, 4267858574, 4267858576, 4267858577, 4267858580, 4267858582, 4267858584, 4267858586, 4267858588, 4267860390, 4267860392, 4267860392, 4267860389, 4267858587, 4267858585, 4267858583, 4267858581, 4267858579, 4267858578, 4267858575, 2260151181, 4267858573, 4267858572, 4266292180, 4266292179, 4266292177, 1453758546, 2256253962, 4266292173, 4266292171, 272268719, 4266292170, 2254767162, 4266292168, 2254765685, 4266292165, 2254759816, 4266292163, 2254759819, 4266292161, 2254759793, 4264566381, 2254727624, 4264566379, 2254467886, 4264566376, 2254705079, 4264566374, 2254698801, 4264566371, 2254691954, 4264566370, 2254683065, 4264566367, 2254680005, 4264566366, 2254674648, 4231826464, 2254667655, 4231826458, 2254663239, 4231826454, 2253533857, 4133111073, 2253470303, 4214707549],[216727321, 216702397, 216272039, 216692990, 424308012, 215947790, 215689220, 215665352, 416138045, 215559000, 215558990, 132924149, 132600976, 67502664, 413591742, 131714613, 131724970, 134621262, 429269873, 391489713, 46228158, 97792351, 97792364, 413505753, 97795603, 97795762, 216727453, 216726435, 216703279, 216703207, 216703112, 216691624, 216691454, 216691171, 216690561, 216622344, 216279158, 216278610, 216277739, 132087627, 216271896, 215665430, 132924166, 414590979, 414590977, 413591731, 131724924, 390550952, 216906045, 216906025, 216906003, 46225424, 134621259, 215331118, 215335861, 44159160, 97790705, 97793255, 97795762, 97795608, 413505752, 390557461, 56723838, 391489712, 97792355, 429269878, 134621264, 131724950, 131724939, 413591743, 8038165, 132924153, 41889937, 215557235, 215557236, 416138046, 215558993, 215665350, 215671643, 429269900, 216072179, 216692999, 216692037, 216727323, 216727321, 97793256, 97790702, 70810210, 215335799, 215331109, 134621277, 46225506, 216905994, 216906024, 216906045, 390550952, 131724921, 413591731, 414590976, 414590978, 132924142, 215665430, 216272037, 132087621, 216277740, 216278465, 216279157, 216622341, 216690560, 216691245, 216691441, 216691722, 216703111, 216703210, 216725958, 216726288, 216727453, 216727321, 216702397, 216272039, 216692990, 424308012, 215942831, 424308014, 215947796, 216727453, 216726435, 216703279, 216703207, 216703112, 216691624, 216691454, 216691171, 216690561, 216622344, 216279158, 216278610, 216277739, 132087627, 216271896],[6170462, 6170461, 6361081]
        // 1996790--Q6362581--Kangaroo Point------[29048218]--[148015863, 380200326, 380200328, 373712078, 380179433]--[]
        Relation r = getRelationByID("9057", rootPath2 + "OSMRelation_Australia.txt");
        System.out.println(r.getId() + "\t" + r.getnodeIDs() + "\t" + r.getwayIDs() + "\t" + r.getrelationIDs());
        System.out.println(relation2WKT(r, rootPath2 + "OSMNode_Australia.txt", rootPath2 + "OSMWay_Australia.txt", rootPath2 + "OSMRelation_Australia.txt"));
        System.out.println("Done!");

    }
}

