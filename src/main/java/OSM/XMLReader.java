package OSM;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.management.relation.Relation;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by SmallApple on 2017/4/17.
 */

public class XMLReader extends DefaultHandler {

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
    private String pointids = "";
    private Nodes nodes;
    private Way way;
    private Relation relation;

    private List<Nodes> nodeslist;
    private List<Way> waylist;
    private List<Relation> relationlist;

    boolean Type = false, temTympe = false;
    private Integer countp = 0;
    private Integer countw = 0;
    private Integer plagN = 0; //用于只提取前两个标签
    private Integer plagW = 0; //用于只提取前两个标签

    @Override
    public void startDocument() throws SAXException {
        nodeslist = new ArrayList<Nodes>();
        waylist = new ArrayList<Way>();
        relationlist = new ArrayList<Relation>();
        System.out.println("正在读取XML(OSM)文档，如果数据量过大需要一段时间，请耐心等待……");
    }
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if("node".equals(qName)) {
            if(attributes.getValue("id") != null && attributes.getValue("id") != "")
                idcontents = attributes.getValue("id");
            else
                idcontents ="0";
            if(attributes.getValue("version") != null && attributes.getValue("version") != "")
                versioncontents = attributes.getValue("version");
            else
                versioncontents = "0";
            if(attributes.getValue("uid") != null && attributes.getValue("uid") != "")
                uidioncontents = attributes.getValue("uid");
            else
                uidioncontents = "0";
            if(attributes.getValue("user") != null && attributes.getValue("user") != "")
                usercontents = attributes.getValue("user");
            else
                usercontents = "0";
            if(attributes.getValue("lon") != null && attributes.getValue("lon") != "")
                loncontents = attributes.getValue("lon");
            else
                loncontents = "0";
            if(attributes.getValue("lat")!=null && attributes.getValue("lat") != "")
                latcontents = attributes.getValue("lat");
            else
                latcontents = "0";
            if(attributes.getValue("changeset") != null && attributes.getValue("changeset") != "")
                changesetcontents = attributes.getValue("changeset");
            else
                changesetcontents = "0";
            if(attributes.getValue("timestamp") != null && attributes.getValue("timestamp") != "")
                timestampcontents = attributes.getValue("timestamp");
            else
                timestampcontents = "0";
            // if(attributes.getValue("visible") != null&&attributes.getValue("visible") != "") //不再存储visible字段
            // visiblecontents = attributes.getValue("visible");
            // else
            // visiblecontents = "0";
            curretntag = "node";
            countp++;
        }
        if("node".equals(curretntag) && "tag".equals(qName) && plagN < 4) {
            String kcontents="";
            String vcontents="";
            kcontents = attributes.getValue("k");
            vcontents = attributes.getValue("v");
            kvcontentsN += kcontents + " = " + vcontents + "; ";
            plagN++; //用于只提取前4个标签
        }

        if("way".equals(qName)) {
            //对way操作
            if(attributes.getValue("id") != null && attributes.getValue("id") != "")
                idcontents = attributes.getValue("id");
            else
                idcontents = "0";
            if(attributes.getValue("version") != null && attributes.getValue("version") != "")
                versioncontents = attributes.getValue("version");
            else
                versioncontents = "0";
            if(attributes.getValue("uid") != null && attributes.getValue("uid") != "")
                uidioncontents = attributes.getValue("uid");
            else
                uidioncontents = "0";
            if(attributes.getValue("user") != null && attributes.getValue("user") != "")
                usercontents = attributes.getValue("user");
            else
                usercontents="0";
            if(attributes.getValue("changeset") != null && attributes.getValue("changeset") != "")
                changesetcontents = attributes.getValue("changeset");
            else
                changesetcontents = "0";
            if(attributes.getValue("timestamp") != null && attributes.getValue("timestamp") != "")
                timestampcontents = attributes.getValue("timestamp");
            else
                timestampcontents = "0";
            // if(attributes.getValue("visible") != null&&attributes.getValue("visible") != "") //不再存储visible字段
            // visiblecontents=attributes.getValue("visible");
            // else
            // visiblecontents = "0";
            curretntag = "way";
            countw++;
        }

        if("way".equals(curretntag) && "tag".equals(qName) && plagW < 2) {
            String kcontents = "";
            String vcontents = "";
            kcontents = attributes.getValue("k");
            vcontents = attributes.getValue("v");
            kvcontentsW += kcontents + " = " + vcontents + "; ";
            plagW++;//用于只提取前两个标签
        }
        //对nd操作
        if("way".equals(curretntag) && "nd".equals(qName)) {
            String ref = "";
            ref = attributes.getValue("ref");
            pointids += ref + ";";
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        //对node进行处理
        if("node".equals(qName)){
            nodes = new Nodes();
            nodes.setId(idcontents);
            nodes.setVersion(versioncontents);
            nodes.setUid(uidioncontents);
            nodes.setUser(usercontents);
            nodes.setLon(loncontents);
            nodes.setLat(latcontents);
            nodes.setChangeset(changesetcontents);
            nodes.setTimestamp(timestampcontents);
            nodes.setVisible(visiblecontents);
            nodes.setTag(kvcontentsN);
            nodeslist.add(nodes);
            //对要存满的waylist进行处理
            if(nodeslist.size() >= 100000)
            {
                for(int i = 0; i<nodeslist.size(); i++){
                    System.out.println("Point Id: " + nodeslist.get(i).getId() + " version: " + nodeslist.get(i).getVersion() + " timestamp: " + nodeslist.get(i).getTimestamp() + " tag: " + nodeslist.get(i).getTag());
                    Long.parseLong(nodeslist.get(i).getId());
                    Integer.parseInt(nodeslist.get(i).getVersion());
                    nodeslist.get(i).getLon();
                    nodeslist.get(i).getLat();
                    Long.parseLong(nodeslist.get(i).getUid());
                    nodeslist.get(i).getUser();
                    nodeslist.get(i).getChangeset();
                    Timestamp.valueOf(nodeslist.get(i).getTimestamp().replace("T", " ").replace("Z", ""));
                    nodeslist.get(i).getVisible();
                    nodeslist.get(i).getTag();
                }
                nodeslist.clear();
            }
            kvcontentsN = "";
            curretntag = "";
            plagN = 0;
            nodes = null;
        }

        //对way进行处理
        if("way".equals(qName)){
            //对前面没有处理完的nodeslist集合进行处理
            if(!nodeslist.isEmpty())
            {
                for(int i = 0; i < nodeslist.size(); i++){
                    System.out.println("Point Id: " + nodeslist.get(i).getId() + " version: " + nodeslist.get(i).getVersion() + " timestamp: " + nodeslist.get(i).getTimestamp() + " tag: " + nodeslist.get(i).getTag());
                    Long.parseLong(nodeslist.get(i).getId());
                    Integer.parseInt(nodeslist.get(i).getVersion());
                    nodeslist.get(i).getLon();
                    nodeslist.get(i).getLat();
                    Long.parseLong(nodeslist.get(i).getUid());
                    nodeslist.get(i).getUser();
                    nodeslist.get(i).getChangeset();
                    Timestamp.valueOf(nodeslist.get(i).getTimestamp().replace("T", " ").replace("Z", ""));
                    nodeslist.get(i).getVisible();
                    nodeslist.get(i).getTag();
                }
                nodeslist.clear();
            }

            way = new Way();
            nodes = new Nodes();
            way.setId(idcontents);
            way.setVersion(versioncontents);
            way.setUid(uidioncontents);
            way.setUser(usercontents);
            way.setChangeset(changesetcontents);
            way.setTimestamp(timestampcontents);
            way.setTag(kvcontentsW);
            way.setPointids(pointids);
            way.setVisible(visiblecontents);

            waylist.add(way);

            //对要存满的waylist进行处理
            if(waylist.size() >= 100000)
            {
                for(int i = 0; i < waylist.size(); i++){
                    try{
                        if(polygonOrPolyline(waylist.get(i).getPointids()))
                            Type = true;//polygon;
                        else
                            Type = false;
                    /*if(Type == true){
                        //poly(id,version,userid,username,changeset,timestamp,visible,tag,pointids,polyType,updateData);
                        System.out.println(pointids);
                    }
                    if(Type == false){
                        //poly(id,version,userid,username,changeset,timestamp,visible,tag,pointids,polyType,updateData);
                        System.out.println(pointids);
                    }*/
                        System.out.println("Poly Id:" + waylist.get(i).getId() + " version: " + waylist.get(i).getVersion() + " tag: " + waylist.get(i).getTag());
                        Long.parseLong(waylist.get(i).getId());
                        Integer.parseInt( waylist.get(i).getVersion());
                        Long.parseLong(waylist.get(i).getUid());
                        waylist.get(i).getUser();
                        waylist.get(i).getChangeset();
                        Timestamp.valueOf(waylist.get(i).getTimestamp().replace("T", " ").replace("Z", ""));
                        waylist.get(i).getVisible();
                        waylist.get(i).getTag();
                        waylist.get(i).getPointids();
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                }
                waylist.clear();
            }
            kvcontentsW = "";
            curretntag = "";
            pointids = "";
            plagW = 0;
            way = null;
        }
    }
    @Override
    public void endDocument() throws SAXException{
        //对前面没有处理完的waylist进行处理
        if(!waylist.isEmpty())
        {
            for(int i = 0; i < waylist.size(); i++){
                try{
                    String strSql = "";
                    if(polygonOrPolyline(waylist.get(i).getPointids()))
                        Type = true; //polygon;
                    else
                        Type = false;
                /*if(Type == true){
                    strSql = "Insert into poly(id,version,userid,username,changeset,timestamp,visible,tag,pointids,polyType,updateData) Values(?,?,?,?,?,?,?,?,?,3,0)";
                }
                if(Type == false){
                    strSql = "Insert into poly(id,version,userid,username,changeset,timestamp,visible,tag,pointids,polyType,updateData) Values(?,?,?,?,?,?,?,?,?,2,0)";
                }*/
                    System.out.println("Poly Id: " + waylist.get(i).getId() + " version: " + waylist.get(i).getVersion() + " tag: " + waylist.get(i).getTag());
                    Long.parseLong(waylist.get(i).getId());
                    Integer.parseInt( waylist.get(i).getVersion());
                    Long.parseLong(waylist.get(i).getUid());
                    waylist.get(i).getUser();
                    waylist.get(i).getChangeset();
                    Timestamp.valueOf(waylist.get(i).getTimestamp().replace("T", " ").replace("Z", ""));
                    waylist.get(i).getVisible();
                    waylist.get(i).getTag();
                    waylist.get(i).getPointids();
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
            waylist.clear();
        }
    }

    private boolean polygonOrPolyline(String nodes){
        //用于区分线和面数据
        //If return true, the poly is a polygon.
        if(nodes.length() < 1)
            return false;
        String[] ss = nodes.split(";");
        if(ss.length < 4)
            return false;
        if(ss[0].equals(ss[ss.length-1]))
            return true;
        else
            return false;
    }

    public void readXML(String filePath) throws ParserConfigurationException, SAXException, IOException {
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
    }
    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException{
        long startTime = System.currentTimeMillis(); //记录开始时间
        Date dateStart = new Date();
        XMLReader readersecond = new XMLReader();
        String filePath = "F:/taiwan-latest.osm";
        readersecond.readXML(filePath);
        long endTime = System.currentTimeMillis(); //记录结束时间
        float excTime = (float)(endTime - startTime) / 1000;
        int hours = (int)(excTime / 3600);
        int minutes = (int)((excTime % 3600) / 60);
        float seconds = (excTime % 3600) % 60;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("完成! 开始于：" + df.format(dateStart) + " 结束于：" + df.format(new Date()) + " 耗时："+ hours+ "时：" +  minutes + "分：" + seconds + "秒！");
        System.exit(0);
    }
}


class Nodes{
    //对应于XML中的node，数据库中的Point
    private String id;
    private String version;
    private String uid;
    private String user;
    private String lon;
    private String lat;
    private String changeset;
    private String timestamp;
    private String visible;
    private String tag;

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
    public String getVisible() {
        return visible;
    }
    public void setVisible(String visible) {
        this.visible = visible;
    }
}
class Way{
    //对应于XML中的way、数据库中的Polylin和Polygon
    private String id;
    private String version;
    private String uid;
    private String user;
    private String changeset;
    private String timestamp;
    private String tag;
    private String point;
    private String pointids;
    private String visible;

    public String getVisible() {
        return visible;
    }
    public void setVisible(String visible) {
        this.visible = visible;
    }
    public String getPointids() {
        return pointids;
    }
    public void setPointids(String pointids) {
        this.pointids = pointids;
    }
    public String getPoint() {
        return point;
    }
    public void setPoint(String point) {
        this.point = point;
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

}


