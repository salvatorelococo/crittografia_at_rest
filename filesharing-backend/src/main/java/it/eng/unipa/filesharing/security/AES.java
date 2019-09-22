package it.eng.unipa.filesharing.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
 
public class AES {
 
    private static SecretKeySpec create(String myKey)
    {
        MessageDigest sha = null;
        try {
            byte[] key = myKey.getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance("SHA-256");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 32);
            return new SecretKeySpec(key, "AES");
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }
 
    public static byte[] encrypt(byte[] content, String secret)
    {
        try
        {

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, create(secret));
            return cipher.doFinal(content);
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }
 
    public static byte[] decrypt(byte[] strToDecrypt, String secret)
    {
        try
        {

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, create(secret));
            return cipher.doFinal(strToDecrypt);
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }

}