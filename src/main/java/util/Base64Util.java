package util;

import java.nio.charset.Charset;
import java.util.Base64;

public class Base64Util {

    public static byte[]  encode(byte[] in) {
        return Base64.getEncoder().encode(in);
    }

    public static String encode(String in, Charset charset) {
        byte[] bytes = Base64.getEncoder().encode(in.getBytes(charset));
        return new String(bytes, charset);
    }

    public static byte[] decode(byte[] in) {
        return Base64.getDecoder().decode(in);
    }

    public static String decode(String in, Charset charset) {
        byte[] bytes = Base64.getDecoder().decode(in.getBytes(charset));
        return new String(bytes, charset);
    }


}
