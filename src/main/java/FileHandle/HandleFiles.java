package FileHandle;
/**
 * Created by SmallApple on 2017/3/18.
 */

import info.monitorenter.cpdetector.io.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static void splitFile(String filePath) throws IOException {
        File file = new File(filePath);
        //获取文件名
        String fileName = file.getName().substring(0, file.getName().indexOf("."));
        //获取文件后缀
        String endName = file.getName().substring(file.getName().lastIndexOf("."));
        System.out.println(endName);
        BufferedReader reader = null;

        int i = 0, j = 0;
        try {
            reader = new BufferedReader(new FileReader(file), 10 * 1024 * 1024);
            String stringMsg = null;
            while ((stringMsg = reader.readLine()) != null) {
                StringBuffer sb = new StringBuffer();
                sb.append(file.getParent()).append("\\").append(fileName)
                        .append("_").append(i/10000+1).append(endName);
                i++;
                WriteFile(sb.toString(), stringMsg + "\r\n");
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    //文件分割的方法（方法内传入要分割的文件路径以及要分割的份数）
    public static void splitFile2(File src, int m) {
        if(src.isFile()) {
            //获取文件的总长度
            long l = src.length();
            //获取文件名
            String fileName = src.getName().substring(0, src.getName().indexOf("."));
            //获取文件后缀
            String endName = src.getName().substring(src.getName().lastIndexOf("."));
            System.out.println(endName);
            InputStream in = null;
            try {
                in = new FileInputStream(src);
                for(int i = 1; i <= m; i++) {
                    StringBuffer sb = new StringBuffer();
                    sb.append(src.getParent()).append("\\").append(fileName)
                            .append("_").append(i).append(endName);
                    System.out.println(sb.toString());
                    File file2 = new File(sb.toString());
                    //创建写文件的输出流
                    OutputStream out = new FileOutputStream(file2);
                    int len = -1;
                    byte[] bytes = new byte[10*1024*1024];
                    //这个根据每次分割的大小不同有所区别
                    while((len = in.read(bytes)) != -1) {
                        out.write(bytes, 0, len);
                        if(file2.length() > (l / m)) {
                            break;
                        }
                    }
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 由于之前的数据文件出现了乱码，因此先对文件进行解码
     * @param str
     * @return
     */
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
    public static void WriteFile(String filePath, String str) {
        File file = new File(filePath);
        try (FileOutputStream fop = new FileOutputStream(file, true)) {
            // 这里设置true是为了向file中追加数据，要不然每次都是自动覆盖原先的数据，所以说，测试运行的时候记得把原来的数据清空
            // if file doesn't exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            // get the str in bytes
            byte[] strInBytes = str.getBytes();
            fop.write(strInBytes);
            fop.flush();
            fop.close();
            //System.out.println("Done");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String string2Unicode(String string) {
        StringBuffer unicode = new StringBuffer();

        for (int i = 0; i < string.length(); i++) {
            // 取出每一个字符
            char c = string.charAt(i);
            // 转换为unicode
            unicode.append("\\u" + Integer.toHexString(c));
        }
        return unicode.toString();
    }
    public static String unicodeToString(String str) {
        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(str);
        char ch;
        while (matcher.find()) {
            ch = (char) Integer.parseInt(matcher.group(2), 16);
            str = str.replace(matcher.group(1), ch + "");
        }
        return str;
    }
    public static String record(int i, String str) {
        /*String buf = "";
        while (str.charAt(i) != '\"' && i<str.length()) { // buf记录下""里面的内容
            buf = buf + str.charAt(i);
            i++;
        }*/
        int j = str.indexOf("\"", i);
        String buf = str.substring(i, j); // buf记录下""里面的内容
        return buf;
    }
}
