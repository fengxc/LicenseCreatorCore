package creator;

import model.License;
import model.SystemInfo;
import test.ExampleLicense;
import util.Base64Util;
import util.JacksonUtils;
import util.RsaKey;
import util.RsaUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;

public class Main {

    public static String OUT_LICENSE_FILE_NAME = "license.txt";
    public static String OUT_LICENSE_INFO_FILE_NAME = "licenseInfo.txt";
    public static String IN_USER_INFO = "info.txt";
    public static String IN_LICENSE_CONFIG = "licenseConfig.txt";

    public static void main(String[] args) throws Exception {
        int argLength = args.length;
        boolean infoBase64 = true;
        for (int i = 0; i < argLength; i++) {
            String arg = args[i];
            System.out.println(i+": "+arg);
        }
        if (argLength >= 4) {
            IN_USER_INFO = args[0];
            IN_LICENSE_CONFIG = args[1];
            OUT_LICENSE_FILE_NAME = args[2];
            OUT_LICENSE_INFO_FILE_NAME = args[3];
            if (argLength >= 5) {
                String base64param = args[4];
                if (base64param != null) {
                    if ("-txt".equals(base64param)) {
                        infoBase64 = false;
                    }
                }
            }
        }

        String str = null;
        if (infoBase64) {
            String base64 = readFile(IN_USER_INFO);
            byte[] bytes = Base64Util.decode(base64.getBytes("UTF-8"));
            str = new String(bytes, "UTF-8");
        }else {
            str =readFile(IN_USER_INFO);
        }
        System.out.println(str);
        SystemInfo info1 = JacksonUtils.toJavaObject(str, SystemInfo.class);
        if (info1.getMacAddressList() != null) {
            info1.getMacAddressList().forEach(System.out::println);
        }
        String licenseConfigJson = readFile(IN_LICENSE_CONFIG);
        License license = JacksonUtils.toJavaObject(licenseConfigJson, License.class);
//        License license = new ExampleLicense().getExample();
//        String exm = JacksonUtils.toJSONString(license);
//
//        printToFile(IN_LICENSE_CONFIG, exm.getBytes(Charset.forName("UTF-8")));
        license.setMacAddress(info1.getMacAddressList());
        license.setCpuSerial(info1.getCpuSerial());
        license.setMainBoardSerial(info1.getMainBoardSerial());
        license.setCreateDate(new Date());
        String licenseJson = JacksonUtils.toJSONString(license);
        System.out.println(licenseJson);

        printToFile(OUT_LICENSE_INFO_FILE_NAME, licenseJson.getBytes(Charset.forName("UTF-8")));

        String license64 = Base64Util.encode(licenseJson, Charset.forName("UTF-8"));
        System.out.println(license64);
        if (license64.getBytes(Charset.forName("UTF-8")).length > 200) {
            FileOutputStream fos = new FileOutputStream(OUT_LICENSE_FILE_NAME);
            byte[] license64Byte = license64.getBytes(Charset.forName("UTF-8"));
            int len = license64Byte.length;
            int i = 0;
            int count = 0;
            while ((i + 200) < len) {
                byte[] copy = new byte[200];
                System.arraycopy(license64Byte, i, copy, 0, 200);
                String rsaResultString = RsaUtil.rsaEncrypt(RsaKey.PRIVATE_KEY, copy, "privateKey");
                System.out.println(count + ": " + rsaResultString);
                fos.write(rsaResultString.getBytes());
                fos.write("\n".getBytes());
                count++;
                i += 200;
            }
            int rest = len - i;
            byte[] last = new byte[rest];
            System.arraycopy(license64Byte, i, last, 0, rest);
            String rsaResultString = RsaUtil.rsaEncrypt(RsaKey.PRIVATE_KEY, last, "privateKey");
            System.out.println(count + ": " + rsaResultString);
            fos.write(rsaResultString.getBytes());
            fos.close();
        } else {
            String rsaResultString = RsaUtil.rsaEncrypt(RsaKey.PRIVATE_KEY, license64, "privateKey");
            printToFile(OUT_LICENSE_FILE_NAME, rsaResultString.getBytes());
        }
    }

    private static String readFile(String filePath) throws IOException {
        File userInfoFile = new File(filePath);
        FileInputStream fis = new FileInputStream(userInfoFile);
        StringBuilder sb = new StringBuilder();
        int temp = 0;
        //当temp等于-1时，表示已经到了文件结尾，停止读取
        while ((temp = fis.read()) != -1) {
            sb.append((char) temp);
        }
        String base64 = sb.toString();
        return base64;
    }

    private static void printToFile(String name, byte[] exm) throws IOException {
        FileOutputStream c = new FileOutputStream(name);
        c.write(exm);
        c.close();
    }
}