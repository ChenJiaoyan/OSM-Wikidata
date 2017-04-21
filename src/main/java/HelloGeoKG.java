/**
 * Created by John on 3/15/17.
 */
import FileHandle.HandleFiles;
import info.monitorenter.cpdetector.io.*;

import java.io.*;

public class HelloGeoKG {
    /*//getcode函数用于识别字符串的字符编码
    public static String getcode(String str) {
        String[] encodelist = {"GB2312", "ISO-8859-1", "UTF-8", "GBK", "gb 18030", "Big5", "UTF-16LE", "Shift_JIS", "EUC-JP", "ISO-2002-JP"};
        for (int i = 0; i < encodelist.length; i++) {
            try {
                if (str.equals(new String(str.getBytes(encodelist[i]), encodelist[i]))) {
                    return encodelist[i];
                }
            } catch (Exception e) {
            } finally {
            }
        }
        return "";
    }*/
    public static boolean IDMatch (String WikiID, String WikiCSV) {
        File file = new File(WikiCSV);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(WikiCSV), 10 * 1024 * 1024);
            String stringLine = null;
            while ((stringLine = reader.readLine()) != null) {
                if (stringLine.indexOf(WikiID) >= 0) {
                    return true;
                }
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return false;
    }

    public static void join (String OSMFile, String WikiFile, String triplePath, String joinPath) throws IOException {
        File OSMfile = new File(OSMFile);
        File Wikifile = new File(WikiFile);
        BufferedReader reader_OSM = null;
        BufferedReader reader_Wiki = null;
        try {
            reader_OSM = new BufferedReader(new FileReader(OSMfile), 10 * 1024 * 1024);
            reader_Wiki = new BufferedReader(new FileReader(Wikifile), 10 * 1024 * 1024);
            String stringLine = null;
            String newstr = null;
            while ((stringLine = reader_OSM.readLine()) != null) {
                int start = stringLine.indexOf(",Q") + 1;
                newstr = stringLine.substring(start); //newstr记录的是OSM数据中的Wikidata ID
                if(IDMatch(newstr, WikiFile)) {
                    System.out.println(stringLine);
                    //HandleFiles.WirteFile(triplePath, stringLine + "\r\n");
                    HandleFiles.WriteFile(joinPath, newstr + "\r\n"); //joinPath会存下OSM和Wikidata做union时的重合部分
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void union (String OSMFile, String WikiFile, String JoinFile, String triplePath) throws IOException {
        File OSMfile = new File(OSMFile);
        File Wikifile = new File(WikiFile);
        File Joinfile = new File(JoinFile);
        BufferedReader reader_OSM = null;
        BufferedReader reader_Wiki = null;
        BufferedReader reader_Join = null;
        try {
            reader_OSM = new BufferedReader(new FileReader(OSMfile), 10 * 1024 * 1024);
            reader_Wiki = new BufferedReader(new FileReader(Wikifile), 10 * 1024 * 1024);
            reader_Join = new BufferedReader(new FileReader(Joinfile), 10 * 1024 * 1024);
            String stringLine = null;
            String wikistr = null;
            while ((stringLine = reader_OSM.readLine()) != null) {
                System.out.println(stringLine);
                HandleFiles.WriteFile(triplePath, stringLine + "\r\n");
            }
            while ((wikistr = reader_Wiki.readLine()) != null) {
                if(!IDMatch(wikistr, JoinFile)) {
                    System.out.println(wikistr);
                    HandleFiles.WriteFile(triplePath, wikistr + "\r\n");
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main (String args[]) throws IOException {
        System.out.printf("This project is to study (1) the linkage between OSM and WikiData, " +
                "(2) the linkage between OSM and POIs \n");
        System.out.println("Have Fun!");
        String str = "康壽養生素食館&#10;(康壽國小側門)&#10;電話：";
        System.out.println(str);
        str = HandleFiles.unicodeToString(str);
        System.out.println(str);
        String OSMFile_China = "F:\\OSMwithWiki_China.csv";
        String OSMFile_Taiwan = "F:\\OSMwithWiki_Taiwan.csv";
        //String WikiFile_China = "F:\\WikiwithOSM_China.csv";
        //String WikiFile_Taiwan = "F:\\WikiwithOSM_Taiwan.csv";
        String WikiFile = "F:\\WikiwithOSM.csv";
        String triplePath_China = "F:\\OSM-Wikidata_China.csv";
        String triplePath_Taiwan = "F:\\OSM-Wikidata_Taiwan.csv";
        String joinPath_China = "F:\\OSM-Wikidata_Join_China.csv";
        String joinPath_Taiwan = "F:\\OSM-Wikidata_Join_Taiwan.csv";
        String unionPath_China = "F:\\OSM-Wikidata_Union_China.csv";
        String unionPath_Taiwan = "F:\\OSM-Wikidata_Union_Taiwan.csv";
        //join(OSMFile_Taiwan, WikiFile, triplePath_Taiwan,joinPath_Taiwan);
        //join(OSMFile_China, WikiFile, triplePath_China,joinPath_China);
        //union (OSMFile_Taiwan, WikiFile, joinPath_Taiwan, unionPath_Taiwan);
        //union (OSMFile_China, WikiFile, joinPath_China, unionPath_China);
        //String str = "member type=\"node\" ref=";
        //System.out.println(str.length());
    }
}
