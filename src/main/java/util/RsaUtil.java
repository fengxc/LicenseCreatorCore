package util;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class RsaUtil {
    public static final String KEY_TYPE_PRIVATE_KEY = "privateKey";
    public static final String KEY_TYPE_PUBLIC_KEY = "publicKey";

    /**
     * 生成公钥和私钥
     *
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static Map<String, String> generateKey() throws NoSuchAlgorithmException {
        Map<String, String> resultMap = new HashMap<>();
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        Base64.Encoder encoder = Base64.getEncoder();
        resultMap.put(KEY_TYPE_PRIVATE_KEY, encoder.encodeToString(keyPair.getPrivate().getEncoded()));
        resultMap.put(KEY_TYPE_PUBLIC_KEY, encoder.encodeToString(keyPair.getPublic().getEncoded()));
        return resultMap;
    }

    /**
     * RSA加密
     * @param key
     * @param content
     * @param keyType
     * @return
     * @throws Exception
     */
    public static String rsaEncrypt(String key, String content, String keyType) throws Exception {
        return rsa(key, content.getBytes(), keyType, Cipher.ENCRYPT_MODE);
    }

    public static String rsaEncrypt(String key, byte[]content, String keyType) throws Exception {
        return rsa(key, content, keyType, Cipher.ENCRYPT_MODE);
    }
    /**
     * RSA解密
     * @param key
     * @param content
     * @param keyType
     * @return
     * @throws Exception
     */
    public static String rsaDecrypt(String key, String content, String keyType) throws Exception {
        return rsa(key, Base64.getDecoder().decode(content), keyType, Cipher.DECRYPT_MODE);
    }

    private static String rsa(String key, byte[] content, String keyType, int mode) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        if (KEY_TYPE_PRIVATE_KEY.equals(keyType)) {
            cipher.init(mode, keyFactory.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(key))));
        } else {
            cipher.init(mode, keyFactory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(key))));
        }
        byte[] bytes = cipher.doFinal(content);
        return mode == Cipher.DECRYPT_MODE ? new String(bytes) : Base64.getEncoder().encodeToString(bytes);
    }

    public static void main(String[] args) throws Exception {
        //String content = "大王叫我来巡山呐";

        //生成密钥对
        Map<String, String> keyMap = generateKey();
        System.out.println("私钥：" + keyMap.get(KEY_TYPE_PRIVATE_KEY));
        System.out.println("公钥：" + keyMap.get(KEY_TYPE_PUBLIC_KEY));
//
//        //私钥加密，公钥解密
//        String privateKeyData = rsaEncrypt(keyMap.get(KEY_TYPE_PRIVATE_KEY), content, KEY_TYPE_PRIVATE_KEY);
//        System.out.println("私钥加密：" + privateKeyData);
//        System.out.println("公钥解密：" + rsaDecrypt(keyMap.get(KEY_TYPE_PUBLIC_KEY), privateKeyData, KEY_TYPE_PUBLIC_KEY));
//
//        //公钥加密，私钥解密
//        String publicKeyData = rsaEncrypt(keyMap.get(KEY_TYPE_PUBLIC_KEY), content, KEY_TYPE_PUBLIC_KEY);
//        System.out.println("公钥加密：" + publicKeyData);
//        System.out.println("私钥解密：" + rsaDecrypt(keyMap.get(KEY_TYPE_PRIVATE_KEY), publicKeyData, KEY_TYPE_PRIVATE_KEY));
    }
}
