package Wikidata;

import FileHandle.HandleFiles;

import java.io.*;

/**
 * Created by SmallApple on 2017/3/21.
 */
public class HandleJSONFiles {
    //public static void tripleExt(String filePath, String encode, String WikiPath, String resultPath, String country) throws Exception {
    public static void tripleExt(String filePath, String encode, String resultPath, String extPath, String country) throws Exception {
        //setUp();
        File file = new File(filePath);
        int all = 0;
        int zh = 0;
        int r = 0;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file), 10 * 1024 * 1024);
            String stringLine = null;
            String newstr = null;
            while ((stringLine = reader.readLine()) != null) {
                newstr = new String(stringLine.getBytes(encode), encode).trim();
                int i = newstr.indexOf("id");
                if (i >= 0) {
                    all++; //all记录下wikidata中所有的实体数目
                } else {
                    continue;
                }
                i += 5;
                int j = i;
                String buf = HandleFiles.record(i, newstr); //buf记录下这个entity的id
                /*i = newstr.indexOf(country + "\":{\"language", i);
                if(i > j) {
                    zh++; //zh记录下wikidata中的中文(country)实体数目
                    i += 26 + 2*country.length();
                    j = i;
                    String buf2 = HandleFiles.record(i, newstr); //buf2记录下这个entity的name
                    String newbuf = HandleFiles.unicodeToString(buf2);
                    HandleFiles.WirteFile(WikiPath, buf + "," + newbuf + "\r\n");
                    if(newstr.indexOf("\"id\":\"Q936\"", i) > j) { //如果这个entity有OSM的references
                        r++; //r记录下wikidata中有OSM的references的中文(country)实体数目
                        System.out.println("ID " + buf + "\t" + "Name " + newbuf);
                        HandleFiles.WirteFile(resultPath, buf + "," + newbuf + "\r\n");
                    }
                }
                else {
                    continue;
                }*/


                /**
                 * 中间文件生成, 生成Wiki-Name_EN&&ID.csv
                 * 记录下wikidata中的ID及其对应的英文名，以便最后生成rdf，因为OSM中有些实体只有中文名
                 * /
                /*
                String namebuf = "\"" + country +"\":{\"language\":\"" + country + "\",\"value\":\"";
                int len = newstr.indexOf(namebuf);
                if(len >= 0) {
                    zh++; //zh记录下wikidata中某一country的实体中有OSM的references的实体数目
                    len += namebuf.length();
                    String buf2 = HandleFiles.record(len, newstr); //buf2记录下这个属于某一country的entity的name
                    String newbuf = HandleFiles.unicodeToString(buf2);
                    System.out.println("ID " + buf + "\tName " + newbuf);
                    HandleFiles.WriteFile("F:\\Wiki-Name_EN&&ID.csv", buf + "," + newbuf + "\r\n");
                }*/

                if(newstr.indexOf("\"id\":\"Q936\"", i) > j) { //如果这个entity有OSM的references
                    HandleFiles.WriteFile(extPath, stringLine + "\r\n");
                    r++; //r记录下wikidata中有OSM的references的实体数目
                    String namebuf = "\"" + country +"\":{\"language\":\"" + country + "\",\"value\":\"";
                    int len = newstr.indexOf(namebuf);
                    if(len >= 0) {
                        zh++; //zh记录下wikidata中某一country的实体中有OSM的references的实体数目
                        len += namebuf.length();
                        String buf2 = HandleFiles.record(len, newstr); //buf2记录下这个属于某一country的entity的name
                        String newbuf = HandleFiles.unicodeToString(buf2);
                        System.out.println("ID " + buf + "\tName " + newbuf);
                        HandleFiles.WriteFile(resultPath, buf + "," + newbuf + "\r\n");
                    }
                }
            }
            reader.close();
        }
        catch(FileNotFoundException e){
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        //String filePath = "F:\\Data\\Wikidata\\first_100_lines.json";
        String filePath = "F:\\Data\\Wikidata\\wikidata-20170102-all.json";
        //String encode = HandleFiles.getFileEncode(filePath);
        String encode = "UTF-8";
        /*// 这是读取json的数据文件操作
        try {
            readFilesByStringBuffer(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        // 这是对json的数据文件进行处理的输出结果
        //String txtPath = "F:\\WikidataTest.txt";
        //String WikiPath1 = "F:\\Wikidata_China1.csv";
        //String WikiPath2 = "F:\\Wikidata_Taiwan1.csv";
        String resultPath1 = "F:\\WikiwithOSM_China.csv";
        String resultPath2 = "F:\\WikiwithOSM_Taiwan.csv";
        String resultPath = "F:\\WikiwithOSM.csv";
        //String extPath = "F:\\Wikidata_ChinaWithOSM.json";
        String extPath = "F:\\WikidataWithOSM.json";
        try {
            //tripleExt(filePath, encode, WikiPath1, resultPath1, "zh-cn");
            //tripleExt(filePath, encode, WikiPath2, resultPath2, "zh-tw");

            //tripleExt(filePath, encode, resultPath1, extPath, "zh-cn");
            //tripleExt(filePath, encode, resultPath2, extPath, "zh-tw");

            tripleExt(filePath, encode, resultPath, extPath, "en");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
