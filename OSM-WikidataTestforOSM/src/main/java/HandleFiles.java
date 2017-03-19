/**
 * Created by SmallApple on 2017/3/18.
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import info.monitorenter.cpdetector.io.*;

public class HandleFiles {
    /**
     * 首先进行文件读取，有三种方式
     * 但是由于我们的数据太大，要尽可能地减少内存占用，因此选择way3 使用cache进行读取
     * @param filePath
     */
    // Way1. 使用commons-io.jar包的FileUtils的类进行读取
    public static void readFilesByFileUtils(String filePath) {
        File file = new File(filePath);
        try {
            LineIterator lineIterator = FileUtils.lineIterator(file, "UTF-8");
            while (lineIterator.hasNext()) {
                String line = lineIterator.nextLine();
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Way2. 使用Scanner进行读取
    public static void readFilesByScanner(String filePath) {
        FileInputStream fileInputStream = null;
        Scanner scanner = null;

        try {
            fileInputStream = new FileInputStream(filePath);
            scanner = new Scanner(fileInputStream, "UTF-8");
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                System.out.println(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (scanner != null) {
                scanner.close();
            }
        }

    }

    // Way3. 使用cache进行读取
    public static void readFilesByStringBuffer(String filePath) throws IOException {
        File file = new File(filePath);

        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(file), 10 * 1024 * 1024);
            String stringMsg = null;
            while ((stringMsg = reader.readLine()) != null) {
                System.out.println(stringMsg);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 由于之前的数据文件出现了乱码，因此先对文件进行解码
     * @param str
     * @return
     */
    //getcode函数用于识别字符串的字符编码
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
    }

    /**
     * 利用第三方开源包cpdetector获取文件编码格式
     * 需要引入antlr.jar、chardet.jar包，maven仓库里没有，需要手动配置
     */
    public static String getFileEncode(String path) {
        CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();
        detector.add(new ParsingDetector(false));
        detector.add(JChardetFacade.getInstance());// 用到antlr.jar、chardet.jar
        // ASCIIDetector用于ASCII编码测定
        detector.add(ASCIIDetector.getInstance());
        // UnicodeDetector用于Unicode家族编码的测定
        detector.add(UnicodeDetector.getInstance());
        java.nio.charset.Charset charset = null;
        File f = new File(path);
        try {
            charset = detector.detectCodepage(f.toURI().toURL());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (charset != null)
            return charset.name();
        else
            return null;
    }

    //public static String[][] infoExtByStringBuffer(String filePath, String encode) throws IOException {
    public static void infoExtByStringBuffer(String filePath, String encode) throws IOException {
        String[][] twoTripleOSM = null;
        int j = 0;
        int k = 2;
        File file = new File(filePath);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file), 10 * 1024 * 1024);
            String stringLine = null;
            String newstr = null;
            while ((stringLine = reader.readLine()) != null) {
                newstr = new String(stringLine.getBytes(encode), encode).trim();
                if (newstr.length() > 7 && newstr.substring(1, 7).equals("way id")) {
                    int i = 9;
                    //String buf = null;
                    String buf = "";
                    while (newstr.charAt(i) != '\"') {
                        buf = buf + newstr.charAt(i);
                        i++;
                    }
                    //twoTripleOSM[j++][0] = buf;
                    if(k == 2) {
                        System.out.println("WayID " + buf);
                    }
                    k = 1;
                }
                if (newstr.length() > 12 && newstr.substring(1, 12).equals("relation id")) {
                    int i = 14;
                    //String buf = null;
                    String buf = "";
                    while (newstr.charAt(i) != '\"') {
                        buf = buf + newstr.charAt(i);
                        i++;
                    }
                    //twoTripleOSM[j++][0] = buf;
                    if(k == 2) {
                        System.out.println("RelationID " + buf);
                    }
                    k = 1;
                }
                if (newstr.length() > 17 && newstr.substring(1, 17).equals("tag k=\"name\" v=\"")) {
                    int i = 17;
                    k = 2;
                    //String buf = null;
                    String buf = "";
                    while (newstr.charAt(i) != '\"') {
                        buf = buf + newstr.charAt(i);
                        i++;
                    }
                    //twoTripleOSM[j++][1] = buf;
                    System.out.println("Name " + buf);
                    k = 2;
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
        }
        //return twoTripleOSM;
    }

    public static void main(String[] args) throws IOException {
        String filePath = "F:\\Data\\OSM\\cambodia-latest.osm";
        String encode = getFileEncode(filePath);
        // 这是读取osm的数据文件操作
        /*try {
            readFilesByStringBuffer(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        // 这是对osm的数据文件进行处理的输出结果
        try {
            HandleFiles.infoExtByStringBuffer(filePath, encode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
