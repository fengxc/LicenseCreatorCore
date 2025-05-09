package util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class AesUtil {


    /**
     * 生成密钥
     *
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String generateSingleKey(String strKey) throws NoSuchAlgorithmException {

        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG" );
        secureRandom.setSeed(strKey.getBytes());
        keyGenerator.init(128,secureRandom);
        SecretKey secretKey = keyGenerator.generateKey();
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(secretKey.getEncoded());
    }

    public static void main(String[] args) {
        try {
            String key = generateSingleKey("QONE8@2025");
            System.out.println(key);

            Cipher cipher1 = Cipher.getInstance("AES");
            SecretKeySpec keySpec = new SecretKeySpec(Base64.getDecoder().decode(key), "AES");
            cipher1.init(Cipher.ENCRYPT_MODE, keySpec);
            String text = "Hello World12345";
            System.out.println(text.length());
            byte[] plainText = text.getBytes();
            byte[] encrypted = cipher1.doFinal(plainText);
            String encodedEncrypted = Base64.getEncoder().encodeToString(encrypted);
            System.out.println("Encrypted: " + encodedEncrypted);

            byte [] toBeDecrypt = Base64.getDecoder().decode(encodedEncrypted);
            Cipher cipher2 = Cipher.getInstance("AES");
            SecretKeySpec keySpec2 = new SecretKeySpec(Base64.getDecoder().decode(key), "AES");
            cipher2.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decrypted = cipher2.doFinal(toBeDecrypt);
            System.out.println(new String(decrypted));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
