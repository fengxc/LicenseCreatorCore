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
import java.util.Base64;
import java.util.Date;

public class Main {
    public static void main(String[] args) throws Exception {

        System.out.println("Hello world!");
        File info = new File("D:\\info.txt");
        if (info.exists()) {
            FileInputStream fis = new FileInputStream(info);
            if (fis!=null){
                StringBuilder sb = new StringBuilder();
                int temp = 0;
                //当temp等于-1时，表示已经到了文件结尾，停止读取
                while ((temp = fis.read()) != -1) {
                    sb.append((char) temp);
                }
                String base64 = sb.toString();
                byte[] bytes = Base64Util.decode(base64.getBytes("UTF-8"));
                String str = new String(bytes, "UTF-8");
                System.out.println(str);
                SystemInfo info1 = JacksonUtils.toJavaObject(str, SystemInfo.class);
                info1.getMacAddressList().forEach(System.out::println);
                License license = new ExampleLicense().getExample();
                String exm = JacksonUtils.toJSONString(license);

                printToFile("D:\\licenseConfig.txt", exm.getBytes(Charset.forName("UTF-8")));
                license.setMacAddress(info1.getMacAddressList());
                license.setCreateDate(new Date());
                String licenseJson  = JacksonUtils.toJSONString(license);
                System.out.println(licenseJson);

                printToFile("D:\\licenseInfo.txt",  licenseJson.getBytes(Charset.forName("UTF-8")));

                String license64 = Base64Util.encode(licenseJson, Charset.forName("UTF-8"));
                System.out.println(license64);
                if (license64.getBytes(Charset.forName("UTF-8")).length > 200) {
                    FileOutputStream fos = new FileOutputStream("D:\\license.txt");
                    byte[] license64Byte = license64.getBytes(Charset.forName("UTF-8"));
                    int len = license64Byte.length;
                    int i = 0;
                    int count = 0;
                    while ((i+200) < len) {
                        byte[] copy = new byte[200];
                        System.arraycopy(license64Byte, i, copy, 0, 200);
                        String rsaResultString = RsaUtil.rsaEncrypt(RsaKey.PRIVATE_KEY, copy, "privateKey");
                        System.out.println(count+": "+rsaResultString);
                        fos.write(rsaResultString.getBytes());
                        fos.write("\n".getBytes());
                        count++;
                        i += 200;
                    }
                    int rest = len-i;
                    byte[] last = new byte[rest];
                    System.arraycopy(license64Byte, i, last, 0, rest);
                    String rsaResultString = RsaUtil.rsaEncrypt(RsaKey.PRIVATE_KEY, last, "privateKey");
                    System.out.println(count+": "+rsaResultString);
                    fos.write(rsaResultString.getBytes());
                    fos.close();
                }else {
                    String rsaResultString = RsaUtil.rsaEncrypt(RsaKey.PRIVATE_KEY, license64, "privateKey");
                    printToFile("D:\\out.txt", rsaResultString.getBytes());
                }
            }
        }
    }

    private static void printToFile(String name, byte[] exm) throws IOException {
        FileOutputStream c = new FileOutputStream(name);
        c.write(exm);
        c.close();
    }
}