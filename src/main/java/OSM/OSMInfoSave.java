package OSM;

import FileHandle.HandleFiles;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by SmallApple on 2017/4/15.
 */
public class OSMInfoSave {

    // 将node的经纬度、way的node reference(暂时是node ID)、relation的node&way&relation的reference(暂时是node ID和way ID以及relation ID)保存下来
    public static void OSMFileSave(String filePath, String encode, String resultPath, String nodePath, String wayPath,  String relationPath, String key) throws Exception {
        File file = new File(filePath);
        BufferedReader reader = null;
        String[] feature = {"node", "way", "relation"};
        int i,j,k,l;
        try {
            reader = new BufferedReader(new FileReader(file), 10 * 1024 * 1024);
            String stringLine = null;
            String newstr = null;
            for(i=0; i<3; i++) { //为了保持OSM文件的XML格式，最前面三行的内容需加上
                stringLine = reader.readLine();
                HandleFiles.WriteFile(resultPath, stringLine + "\r\n");
            }
            while ((stringLine = reader.readLine()) != null) {
                ArrayList strGroup = new ArrayList();
                newstr = new String(stringLine.getBytes(encode), encode).trim();
                for(int r=0; r<3; r++) {
                    // China数据运行的时候，r=0（node）这部分运行结束之后，way和relation的数据存储卡壳了，只有把r设成从1开始才能得到way和relation的数据
                    // 但是"OSMwithWiki_China.osm"可以正常跑出来，不知为何
                    if(newstr.indexOf(feature[r] + " id=\"") > 0) {
                        i = feature[r].length() + 6;
                        String buf = HandleFiles.record(i, newstr); //buf记录下node id/way id/relation id
                        String lat;
                        String lon;
                        String ref = null;
                        ArrayList posGroup = new ArrayList();
                        while(newstr.indexOf("<tag k=\"" + key + "\" v=\"") < 0 && newstr.indexOf("</" + feature[r] + ">") < 0) {
                            strGroup.add(stringLine + "\r\n");
                            if (newstr.indexOf("<tag k=\"") < 0) {
                                if(r == 0 && newstr.indexOf("node id=\"") >= 0) { //如果是node，记录下它的id和经纬度
                                    i = feature[r].length() + 6;
                                    buf = HandleFiles.record(i, newstr); //buf记录下node id
                                    i = newstr.indexOf("lat=", i) + 5;
                                    lat = HandleFiles.record(i, newstr);
                                    i = newstr.indexOf("lon=", i) + 5;
                                    lon = HandleFiles.record(i, newstr);
                                    posGroup.add(buf + "\t\"" + lat + " " + lon + "\"" + "\r\n");//将node的id和经纬度记录下来
                                }
                                if(r == 1 && (j = newstr.indexOf("nd ref=\"")) >= 0) { //如果是way，记录下它的引用节点
                                    i = j + 8;
                                    ref = HandleFiles.record(i, newstr);
                                    posGroup.add(ref);
                                }
                                if(r == 2) { //如果是relation，记录下它的引用节点、路径、关系
                                    if((j = newstr.indexOf("member type=\"node\" ref=\"")) >= 0) {
                                        i = j + 24;
                                        ref = HandleFiles.record(i, newstr);
                                        posGroup.add(ref); //将relation的引用node记录在posGroup字符数组中
                                    }
                                    if((k = newstr.indexOf("member type=\"way\" ref=\"")) >= 0) {
                                        i = k + 23;
                                        ref = HandleFiles.record(i, newstr);
                                        posGroup.add(ref); //将relation的引用way记录在posGroup字符数组中
                                    }
                                    if((l = newstr.indexOf("member type=\"relation\" ref=\"")) >= 0) {
                                        i = l + 28;
                                        ref = HandleFiles.record(i, newstr);
                                        posGroup.add(ref); //将relation的引用relation记录在posGroup字符数组中
                                    }
                                }
                            }
                            stringLine = reader.readLine();
                            newstr = new String(stringLine.getBytes(encode), encode).trim();
                        }
                        if(newstr.indexOf("<tag k=\"" + key + "\" v=\"") == 0) { //如果这些node id/way id/relation id确实有这个key
                            for(i=0; i<strGroup.size(); i++) {
                                HandleFiles.WriteFile(resultPath, (String) strGroup.get(i));
                            }
                            if(r == 0) {
                                int size = posGroup.size();
                                for(i=0; i< size; i++) {
                                    System.out.print(posGroup.get(i));
                                    HandleFiles.WriteFile(nodePath, (String) posGroup.get(i));//将node的经纬度记录下来
                                }
                            }
                            if(r == 1) {
                                System.out.print(buf + "\t");
                                HandleFiles.WriteFile(wayPath, buf + "\r\t");
                                int size = posGroup.size();
                                for(i=0; i< size - 1; i++) {
                                    System.out.print(posGroup.get(i) + ",");
                                    HandleFiles.WriteFile(wayPath, posGroup.get(i) + ",");//将way的node reference记录下来
                                }
                                System.out.println(posGroup.get(size-1));
                                HandleFiles.WriteFile(wayPath, posGroup.get(size-1) + "\r\n");//将way的node reference记录下来
                            }
                            if(r == 2) {
                                System.out.print(buf + "\t");
                                HandleFiles.WriteFile(relationPath, buf + "\r\t");
                                int size = posGroup.size();
                                for(i=0; i< size - 1; i++) {
                                    System.out.print(posGroup.get(i) + ",");
                                    HandleFiles.WriteFile(relationPath, posGroup.get(i) + ",");//将way的node reference记录下来
                                }
                                System.out.println(posGroup.get(size-1));
                                HandleFiles.WriteFile(relationPath, posGroup.get(size-1) + "\r\n");//将way的node reference记录下来
                            }
                            while(newstr.indexOf("</" + feature[r] + ">") < 0) {
                                stringLine = reader.readLine();
                                HandleFiles.WriteFile(resultPath, stringLine + "\r\n");
                                newstr = new String(stringLine.getBytes(encode), encode).trim();
                            }
                        }
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

    public static void OSMFileSave2(String filePath, String encode, String resultPath, String key) throws Exception {
        File file = new File(filePath);
        BufferedReader reader = null;
        String[] feature = {"node", "way", "relation"};
        int i,j,k,l;
        try {
            reader = new BufferedReader(new FileReader(file), 10 * 1024 * 1024);
            String stringLine = null;
            String newstr = null;
            for(i=0; i<3; i++) { //为了保持OSM文件的XML格式，最前面三行的内容需加上
                stringLine = reader.readLine();
                HandleFiles.WriteFile(resultPath, stringLine + "\r\n");
                System.out.println(stringLine);
            }
            ArrayList strGroup = new ArrayList();
            while ((stringLine = reader.readLine()) != null) {
                /*newstr = new String(stringLine.getBytes(encode), encode).trim();
                if(newstr.indexOf("node id=\"4745800228\"") > 0) {
                    while (newstr.indexOf("<way id=") < 0) {
                        HandleFiles.WriteFile("F:\\haha.txt", stringLine + "\r\n");
                        stringLine = reader.readLine();
                        //System.out.println(stringLine);
                        newstr = new String(stringLine.getBytes(encode), encode).trim();
                    }
                    HandleFiles.WriteFile("F:\\haha.txt", stringLine);
                    System.out.println("Done!");
                }*/

                for(int r=0; r<3; r++) {
                    // China数据运行的时候，r=0（node）这部分运行结束之后，way和relation的数据存储卡壳了，只有把r设成从1开始才能得到way和relation的数据
                    // 但是"OSMwithWiki_China.osm"可以正常跑出来，不知为何
                    newstr = new String(stringLine.getBytes(encode), encode).trim();
                    if(newstr.indexOf(feature[r] + " id=\"") > 0) {
                        while(newstr.indexOf("<tag k=\"" + key + "\" v=\"") < 0 && newstr.indexOf("</" + feature[r] + ">") < 0) {
                            strGroup.add(stringLine + "\r\n");
                            stringLine = reader.readLine();
                            newstr = new String(stringLine.getBytes(encode), encode).trim();
                        }
                        if(newstr.indexOf("<tag k=\"" + key + "\" v=\"") == 0) { //如果这些node id/way id/relation id确实有这个key
                            for(i=0; i<strGroup.size(); i++) {
                                HandleFiles.WriteFile(resultPath, (String) strGroup.get(i));
                                System.out.print((String) strGroup.get(i));
                            }
                            strGroup.clear();
                            HandleFiles.WriteFile(resultPath, stringLine + "\r\n");
                            System.out.println(stringLine);
                            while(newstr.indexOf("</" + feature[r] + ">") < 0) {
                                stringLine = reader.readLine();
                                HandleFiles.WriteFile(resultPath, stringLine + "\r\n");
                                System.out.println(stringLine);
                                newstr = new String(stringLine.getBytes(encode), encode).trim();
                            }
                        }
                    }
                    strGroup.clear();
                }
                if(stringLine.equals("</osm>")) { //为了保持OSM文件的XML格式，最后一行的内容需加上
                    HandleFiles.WriteFile(resultPath, "</osm>");
                    System.out.println("</osm>");
                    break;
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void OSMFileHandle(String filePath, String encode, String resultPath, String nodePath, String wayPath,  String relationPath, String key) throws Exception {
        File file = new File(filePath);
        BufferedReader reader = null;
        String[] feature = {"node", "way", "relation"};
        String buf;
        int i;
        try {
            reader = new BufferedReader(new FileReader(file), 10 * 1024 * 1024);
            String stringLine = null;
            String newstr = null;
            while ((stringLine = reader.readLine()) != null) {
                newstr = new String(stringLine.getBytes(encode), encode).trim();
                for(int r=1; r<3; r++) {
                    if(newstr.indexOf(feature[r] + " id=\"") > 0) {
                        String ref;
                        ArrayList posGroup = new ArrayList();
                        while(newstr.indexOf("<tag k=\"" + key + "\" v=\"") < 0 && newstr.indexOf("</" + feature[r] + ">") < 0) {
                            if (newstr.indexOf("<tag k=\"") < 0) {
                                i = feature[r].length() + 6;
                                buf = HandleFiles.record(i, newstr); //buf记录下node id/way id/relation id
                                if(r == 1) { //如果是way，将它的引用节点的ID全部转换成对应的经纬度
                                    int j = newstr.indexOf("nd ref=\"");
                                    while((j = newstr.indexOf("nd ref=\"")) >= 0) {
                                        i = j + 8;
                                        ref = HandleFiles.record(i, newstr);
                                        posGroup.add(ref);
                                        stringLine = reader.readLine();
                                        newstr = new String(stringLine.getBytes(encode), encode).trim();
                                    }
                                    all2lan_lon(nodePath, posGroup);
                                    System.out.print(buf + "\t");
                                    HandleFiles.WriteFile(wayPath, buf + "\r\t");
                                    int size = posGroup.size();
                                    for(i=0; i< size - 1; i++) {
                                        System.out.print(posGroup.get(i) + ",");
                                        HandleFiles.WriteFile(wayPath, posGroup.get(i) + ",");//将way的node reference记录下来
                                    }
                                    System.out.println(posGroup.get(size-1));
                                    HandleFiles.WriteFile(wayPath, posGroup.get(size-1) +"\r\n");//将way的node reference记录下来
                                }
                                if(r == 2) { //如果是relation，记录下它的引用节点或路径
                                    int j = newstr.indexOf("member type=\"node\" ref=\"");
                                    while((j = newstr.indexOf("member type=\"node\" ref=\"")) >= 0) {
                                        i = j + 24;
                                        ref = HandleFiles.record(i, newstr);
                                        posGroup.add(ref); //将relation的引用node记录在posGroup字符数组中
                                        all2lan_lon(nodePath, posGroup);
                                        stringLine = reader.readLine();
                                        newstr = new String(stringLine.getBytes(encode), encode).trim();
                                    }
                                    int k = newstr.indexOf("member type=\"way\" ref=\"");
                                    while((k = newstr.indexOf("member type=\"way\" ref=\"")) >= 0) {
                                        i = k + 23;
                                        ref = HandleFiles.record(i, newstr);
                                        posGroup.add(ref); //将relation的引用way记录在posGroup字符数组中
                                        all2lan_lon(wayPath, posGroup);
                                        stringLine = reader.readLine();
                                        newstr = new String(stringLine.getBytes(encode), encode).trim();
                                    }
                                    int l = newstr.indexOf("member type=\"relation\" ref=\"");
                                    while((k = newstr.indexOf("member type=\"relation\" ref=\"")) >= 0) {
                                        i = k + 28;
                                        ref = HandleFiles.record(i, newstr);
                                        posGroup.add(ref); //将relation的引用relation记录在posGroup字符数组中
                                        all2lan_lon(relationPath, posGroup);
                                        stringLine = reader.readLine();
                                        newstr = new String(stringLine.getBytes(encode), encode).trim();
                                    }
                                    System.out.print(buf + "\t");
                                    HandleFiles.WriteFile(relationPath, buf + "\t");
                                    int size = posGroup.size();
                                    for(i=0; i< size - 1; i++) {
                                        System.out.print( posGroup.get(i) + ",");
                                        HandleFiles.WriteFile(relationPath, posGroup.get(i) + ",");//将relation的node和way的reference记录下来
                                    }
                                    System.out.println(posGroup.get(size-1));
                                    HandleFiles.WriteFile(relationPath, posGroup.get(size-1) + "\r\n");
                                }
                            }
                            stringLine = reader.readLine();
                            newstr = new String(stringLine.getBytes(encode), encode).trim();
                        }
                    }
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void all2lan_lon(String filePath, ArrayList group) {
        int i,j;
        File file = new File(filePath);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file), 10 * 1024 * 1024);
            String stringLine = null;
            for(i=0; i<group.size(); i++) {
                while ((stringLine = reader.readLine()) != null) {
                    if(stringLine.indexOf((String) group.get(i)) >= 0) {
                        int l = stringLine.indexOf("\"") + 1;
                        group.set(i, HandleFiles.record(l, stringLine));
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
        String encode = HandleFiles.getFileEncode(filePath);
        String rootPath2 = "F:\\SmallApple\\OSM-Wikidata_data\\other\\";
        String resultPath = rootPath2 + "OSMwithWiki_Australia.osm";
        String nodePath = rootPath2 + "OSMNode_Australia.txt";
        String wayPath = rootPath2 + "OSMWay_Australia.txt";
        String relationPath = rootPath2 + "OSMRelation_Australia.txt";

        String key = "wikidata";
        try {
            //OSMFileSave(filePathTaiwan, encodeT, resultPathTaiwan, nodePathTaiwan, wayPathTaiwan, relationPathTaiwan , key);
            //OSMFileSave(filePathChina, encodeC, resultPathChina, nodePathChina, wayPathChina, relationPathChina, key);
            //OSMFileSave2(filePathTaiwan, encodeT, resultPathTaiwan, key);
            //OSMFileSave2(filePathChina, encodeC, resultPathChina, key);
            //OSMFileSave(filePath, encode, resultPath, nodePath, wayPath, relationPath, key);
            OSMFileSave2(filePath, encode, resultPath, key);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
