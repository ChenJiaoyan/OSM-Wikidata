package OSM_Wikidata;

/**
 * Created by SmallApple on 2017/5/25.
 */

import FileHandle.HandleFiles;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.*;

public class Tag extends DefaultHandler {
    private final static String XML_TAG_OSM = "osm";
    private final static String XML_TAG_NODE = "node";
    private final static String XML_TAG_WAY = "way";
    private final static String XML_TAG_ND = "nd";
    private final static String XML_TAG_REF = "ref";
    private final static String XML_TAG_NAME = "name";


    private String curretntag = "";

    private TreeMap tagNum_n;
    private TreeMap tagNum_w;
    private TreeMap tagNum_r;
    private TreeMap tagNum;

    String rootPath = "F:/";
    private String TagQuantityNWR = rootPath + "TagQuantityNWR.txt";
    private String TagQuantityALL = rootPath + "TagQuantityALL.txt";

    //这tagN用于记录含有同一tag的节点/路径/关系数目，每次用完均要清零
    private Integer countn = 0;
    private Integer countw = 0;
    private Integer countr = 0;
    private int wRefN = 0;
    private int rRefN = 0;
    private int rRefW = 0;
    private int rRefR = 0;

    private int tagIF = 0;
    private int readIF = 0;

    @Override
    public void startDocument() throws SAXException {
        if(readIF == 0) {
            tagNum_n = new TreeMap();
            tagNum_w = new TreeMap();
            tagNum_r = new TreeMap();
            tagNum = new TreeMap();
        }
        System.out.println("正在读取XML(OSM)文档，如果数据量过大需要一段时间，请耐心等待……");
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        // 对node进行操作
        if ("node".equals(qName)) { //记录下共享同一tag的node数目
            if(tagIF == 1) {
                countn = 0;
                tagIF = 0;
            }
            curretntag = "node";
            countn++;
        }

        if ("node".equals(curretntag) && "tag".equals(qName)) {
            tagIF = 1;
            String kcontents = attributes.getValue("k");
            if(tagNum_n != null && tagNum_n.containsKey(kcontents)) {
                tagNum_n.put(kcontents, (int)tagNum_n.get(kcontents) + countn);
                System.out.println(kcontents + "\t" + ((int)tagNum_n.get(kcontents) + countn));
            } else if (!tagNum_n.containsKey(kcontents)) {
                tagNum_n.put(kcontents, countn);
                System.out.println(kcontents + "\t" + countn);
            }
            if(tagNum != null && tagNum.containsKey(kcontents)) {
                tagNum.put(kcontents, (int)tagNum.get(kcontents) + countn);
            } else if (!tagNum.containsKey(kcontents)) {
                tagNum.put(kcontents, countn);
            }
        }

        //对way操作
        if ("way".equals(qName)) {
            curretntag = "way";
            countw++;
        }

        if("way".equals(curretntag) && "tag".equals(qName)) {
            String kcontents = attributes.getValue("k");
            if(tagNum_w != null && tagNum_w.containsKey(kcontents)) {
                tagNum_w.put(kcontents, (int)tagNum_w.get(kcontents) + countw);
                System.out.println(kcontents + "\t" + ((int)tagNum_w.get(kcontents) + countw));
            } else if (!tagNum_w.containsKey(kcontents)) {
                tagNum_w.put(kcontents, countw);
                System.out.println(kcontents + "\t" + countw);
            }
            if(tagNum != null && tagNum.containsKey(kcontents)) {
                tagNum.put(kcontents, (int)tagNum.get(kcontents) + countw);
            } else if (!tagNum.containsKey(kcontents)) {
                tagNum.put(kcontents, countw);
            }
        }

        if ("way".equals(curretntag) && "nd".equals(qName)) { //对way的引用node操作
            wRefN++;
            System.out.println("wRefN" + "\t" + wRefN);
        }

        //对relation操作
        if ("relation".equals(qName)) {
            curretntag = "relation";
            countr++;
        }

        if ("relation".equals(curretntag) && "tag".equals(qName)) {
            String kcontents = attributes.getValue("k");
            if(tagNum_r != null && tagNum_r.containsKey(kcontents)) {
                tagNum_r.put(kcontents, (int)tagNum_r.get(kcontents) + countr);
                System.out.println(kcontents + "\t" + ((int)tagNum_r.get(kcontents) + countr));
            } else if (!tagNum_r.containsKey(kcontents)) {
                tagNum_r.put(kcontents, countr);
            }
            if(tagNum != null && tagNum.containsKey(kcontents)) {
                tagNum.put(kcontents, (int)tagNum.get(kcontents) + countr);
            } else if (!tagNum.containsKey(kcontents)) {
                tagNum.put(kcontents, countr);
            }
        }

        if ("relation".equals(curretntag) && "member".equals(qName)) { //对relation引用的node和way操作
            String member = attributes.getValue("type");
            if(member.equals("node")) {
                rRefN++;
                System.out.println("rRefN" + "\t" + rRefN);
            }
            if(member.equals("way")) {
                rRefW++;
                System.out.println("rRefW" + "\t" + rRefW);
            }
            if(member.equals("relation")) {
                rRefR++;
                System.out.println("rRefR" + "\t" + rRefR);
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        /*//对node进行处理
        if ("node".equals(qName)) {
            countn = 0;
        }*/
        //对way进行处理
        if ("way".equals(qName)) {
            countw = 0;
        }
        //对relation进行处理
        if ("relation".equals(qName)) {
            countr = 0;
        }
    }

    @Override
    public void endDocument() throws SAXException {
        System.out.println("wRefN\t" + wRefN);
        System.out.println("rRefN\t" + rRefN);
        System.out.println("rRefW\t" + rRefW);
        System.out.println("rRefR\t" + rRefR);
        HandleFiles.WriteFile(TagQuantityNWR, "wRefN\r\t" + wRefN + "\r\n");
        HandleFiles.WriteFile(TagQuantityNWR, "rRefN\r\t" + rRefN + "\r\n");
        HandleFiles.WriteFile(TagQuantityNWR, "rRefW\r\t" + rRefW + "\r\n");
        HandleFiles.WriteFile(TagQuantityNWR, "rRefR\r\t" + rRefR + "\r\n");
        HandleFiles.WriteFile(TagQuantityALL, "wRefN\r\t" + wRefN + "\r\n");
        HandleFiles.WriteFile(TagQuantityALL, "rRefN\r\t" + rRefN + "\r\n");
        HandleFiles.WriteFile(TagQuantityALL, "rRefW\r\t" + rRefW + "\r\n");
        HandleFiles.WriteFile(TagQuantityALL, "rRefR\r\t" + rRefR + "\r\n");
        System.out.println("tagNum_n");
        HandleFiles.WriteFile(TagQuantityNWR, "tagNum_n" + "\r\n");
        for(int i=0; i<tagNum_n.size(); i++) {
            System.out.println(tagNum_n.keySet().toArray()[i]+ "\t" + tagNum_n.get(tagNum_n.keySet().toArray()[i]));
            HandleFiles.WriteFile(TagQuantityNWR, tagNum_n.keySet().toArray()[i]+ "\r\t" + tagNum_n.get(tagNum_n.keySet().toArray()[i]) + "\r\n");
        }
        System.out.println("tagNum_w");
        HandleFiles.WriteFile(TagQuantityNWR, "tagNum_w" + "\r\n");
        for(int i=0; i<tagNum_w.size(); i++) {
            System.out.println(tagNum_w.keySet().toArray()[i]+ "\t" + tagNum_w.get(tagNum_w.keySet().toArray()[i]));
            HandleFiles.WriteFile(TagQuantityNWR, tagNum_w.keySet().toArray()[i]+ "\r\t" + tagNum_w.get(tagNum_w.keySet().toArray()[i]) + "\r\n");
        }
        System.out.println("tagNum_r");
        HandleFiles.WriteFile(TagQuantityNWR, "tagNum_r" + "\r\n");
        for(int i=0; i<tagNum_r.size(); i++) {
            System.out.println(tagNum_r.keySet().toArray()[i]+ "\t" + tagNum_r.get(tagNum_r.keySet().toArray()[i]));
            HandleFiles.WriteFile(TagQuantityNWR, tagNum_r.keySet().toArray()[i]+ "\r\t" + tagNum_r.get(tagNum_r.keySet().toArray()[i]) + "\r\n");
        }
        for(int i=0; i<tagNum.size(); i++) {
            System.out.println(tagNum.keySet().toArray()[i]+ "\t" + tagNum.get(tagNum.keySet().toArray()[i]));
            HandleFiles.WriteFile(TagQuantityALL, tagNum.keySet().toArray()[i]+ "\r\t" + tagNum.get(tagNum.keySet().toArray()[i]) + "\r\n");
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

    public static void main(String[] args) {
        String rootPath, Area, OSMPath;
        rootPath = "F:\\SmallApple\\OSM-Wikidata_data\\Result\\other\\";
        Area = "Taiwan";
        Area = "China";
        Area = "Australia";
        Area = "Greece";
        OSMPath = rootPath + "OSMwithWiki_" + Area + ".osm";
        String OSMPath_Taiwan = rootPath + "OSMwithWiki_Taiwan.osm";
        String OSMPath_China = rootPath + "OSMwithWiki_China.osm";
        Tag tag = new Tag();
        tag.readOSM(OSMPath);
//
//        tag.readOSM(OSMPath_Taiwan);
//        tag.readIF = 1;
//        tag.readOSM(OSMPath_China);
        System.out.println("done!");
    }
}
