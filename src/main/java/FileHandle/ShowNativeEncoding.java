package FileHandle;

/**
 * Created by SmallApple on 2017/3/20.
 */
public class ShowNativeEncoding {
    public static void main(String[] args) {
        String enc = System.getProperty("file.encoding");
        System.out.println(enc);
    }
}
