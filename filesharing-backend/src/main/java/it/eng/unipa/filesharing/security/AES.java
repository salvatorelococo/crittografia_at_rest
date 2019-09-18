package it.eng.unipa.filesharing.security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
 
public class AES {
 
    private static SecretKeySpec create(String myKey)
    {
        MessageDigest sha = null;
        try {
            byte[] key = myKey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
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

    public static void main(String[] args) {
        byte[] crypted = AES.encrypt("Ciao".getBytes(),"pwd1");
        byte[] decrypted = AES.decrypt(crypted,"pwd2");
        System.out.println(new String(decrypted));
    }
}