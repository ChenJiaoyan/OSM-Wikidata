package OSM;

import FileHandle.HandleFiles;
import java.io.*;
import java.util.ArrayList;

import static FileHandle.HandleFiles.record;
import static FileHandle.HandleFiles.string2Unicode;


/**
 * Created by SmallApple on 2017/3/21.
 */
public class HandleOSMFiles {
    public static String nameMatch (String OSMname, String WikiCSV, String encode) {
        File file = new File(WikiCSV);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(WikiCSV), 10 * 1024 * 1024);
            String stringLine = null;
            String newstr = null;
            while ((stringLine = reader.readLine()) != null) {
                newstr = new String(stringLine.getBytes(encode), encode).trim();
                int i = newstr.indexOf(",");
                int len = newstr.length();
                if(newstr.indexOf(OSMname) > 0) {
                    return newstr.substring(0,i);
                }
            }
            reader.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        //return false;
        return null;
    }

    //public static void infoExt(String filePath, String encode, String OSMPath, String resultPath, String key, String country) throws Exception {
    public static void infoExt(String filePath, String encode, String resultPath, String key, String country) throws Exception {
        //setUp();
        File file = new File(filePath);
        BufferedReader reader = null;
        String[] feature = {"node", "way", "relation"};
        int[][] quantity = {{0, 0, 0}, {0, 0, 0}};
        /*
        quantity[0][0] = 有某一key的node id数量
        quantity[1][0] = node id数量
        quantity[0][1] = 有某一key的way id数量
        quantity[1][1] = way id数量
        quantity[0][2] = 有某一key的relation id数量
        quantity[1][2] = relation id数量
         */
        int i = 0;
        String buf;
        int num = 0;
        try {
            reader = new BufferedReader(new FileReader(file), 10 * 1024 * 1024);
            String stringLine = null;
            String newstr = null;
            while ((stringLine = reader.readLine()) != null) {
                newstr = new String(stringLine.getBytes(encode), encode).trim();
                for(int r=0; r<3; r++) {
                    if(newstr.indexOf(feature[r] + " id") > 0) {
                        quantity[1][r]++;
                        int j = 0;
                        String name = null;
                        String name_en = null;
                        String name_zh = null;
                        ArrayList strGroup = new ArrayList();
                        while(newstr.indexOf("<tag k=\"" + key + "\" v=\"") < 0 && newstr.indexOf("</" + feature[r] + ">") < 0) {
                            if(newstr.indexOf(feature[r] + " id") > 0) {
                                i = feature[r].length() + 6;
                                buf = HandleFiles.record(i, newstr);
                                strGroup.add(buf); //将可能共有相同key的node id/way id/relation id全部记录在strGroup字符数组中
                                j++;
                            }
                            if(newstr.indexOf("<tag k=\"name\" v=\"") >= 0) {
                                name = HandleFiles.record(17, newstr);
                            }
                            /*if(newstr.indexOf("<tag k=\"name:en\" v=\"") >= 0) {
                                name_en = HandleFiles.record(20, newstr);
                            }
                            if(newstr.indexOf("<tag k=\"name:zh\" v=\"") >= 0) {
                                name_zh = HandleFiles.record(20, newstr);
                            }*/
                            stringLine = reader.readLine();
                            newstr = new String(stringLine.getBytes(encode), encode).trim();
                        }
                        if(newstr.indexOf("<tag k=\"" + key + "\" v=\"") == 0) { //如果这些node id/way id/relation id确实有这个key
                            quantity[0][r] += j;
                            num++; // 一个key可能对应着多个id，num是为了记录osm中有多少(wikidata的)key
                            i = 13 + key.length();
                            buf = HandleFiles.record(i, newstr); //buf记录下wikidata ID
                            for(int k=0; k<j; k++) {
                                System.out.println(feature[r] + " ID: " + strGroup.get(k) + "\tName: " + name + "\t" + buf);
                                HandleFiles.WriteFile(resultPath, feature[r] + "," + strGroup.get(k) + "," + name + "," + buf + "\r\n");
                                //System.out.println(feature[r] + " ID: " + strGroup.get(k) + "\tName: " + name + "\tName_en: " + name_en + "\tName_zh:" + name_zh + "\t" + buf);
                                //HandleFiles.WriteFile(resultPath, feature[r] + "," + strGroup.get(k) + "," + name_en + "," + name_zh + "," + buf + "\r\n");
                            }
                        }
                    }
                }
            }
            reader.close();
            HandleFiles.WriteFile("F:/OSM with Wikidata Key.txt", country + "\r\n");
            for(i=0; i<2; i++) {
                for(int j=0; j<3; j++) {
                    HandleFiles.WriteFile("F:/OSM with Wikidata Key.txt", String.valueOf(quantity[i][j]) + "\r\t");
                }
                HandleFiles.WriteFile("F:/OSM with Wikidata Key.txt", "\r\n");
            }
            HandleFiles.WriteFile("F:/OSM with Wikidata Key.txt", String.valueOf(num) + "\r\n");
        } catch (FileNotFoundException e) {
        }
    }

    public static void main(String[] args) throws Exception {
        String filePathTaiwan = "F:\\Data\\OSM\\taiwan-latest.osm";
        String encodeT = HandleFiles.getFileEncode(filePathTaiwan);
        String filePathChina = "F:\\Data\\OSM\\china-latest.osm";
        String encodeC = HandleFiles.getFileEncode(filePathChina);
        /*// 这是读取osm的数据文件操作
        try {
            readFilesByStringBuffer(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        // 这是对osm的数据文件进行处理的输出结果
        //String OSMPathTaiwan = "F:\\OSM_Taiwan.csv";
        //String OSMPathChina = "F:\\OSM_China.csv";
        String resultPathChina = "F:\\OSMwithWiki_China.csv";
        String resultPathTaiwan = "F:\\OSMwithWiki_Taiwan.csv";
        String key = "wikidata";
        //String key = "name";
        try {
            //infoExt(filePathTaiwan, encodeT, OSMPathTaiwan, resultPathTaiwan, "wikidata", "zh-tw");
            //infoExt(filePathChina, encodeC, OSMPathChina, resultPathChina, "wikidata", "zh-cn");
            infoExt(filePathTaiwan, encodeT, resultPathTaiwan, key, "zh-tw");
            infoExt(filePathChina, encodeC, resultPathChina, key, "zh-cn");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
