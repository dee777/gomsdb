/*
Text file AES 암호화 및 복호화
*/

package stata;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import com.stata.sfi.*;

public class crypto {
    public static IvParameterSpec getIv(String key) {
        String iv_sub=key.substring(0,16);
        byte[] iv=iv_sub.getBytes(StandardCharsets.UTF_8);
        return new IvParameterSpec(iv);
    }
    public static String encrypt(String specName, SecretKey key, IvParameterSpec iv,
                                 String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance(specName);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return new String(Base64.getEncoder().encode(encrypted));
    }
    public static String decrypt(String specName, SecretKey key, IvParameterSpec iv,
                                 String cipherText) throws Exception {
        Cipher cipher = Cipher.getInstance(specName);
        cipher.init(Cipher.DECRYPT_MODE, key, iv); // 모드가 다르다.
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(cipherText));
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    public static void File_crypto(String fname, String newfname, String keyvalue) throws Exception {
        SecretKey key = new SecretKeySpec(keyvalue.getBytes(),"AES");
        IvParameterSpec ivParameterSpec = crypto.getIv(keyvalue);
        String specName = "AES/CBC/PKCS5Padding";
        BufferedReader reader=new BufferedReader(
                new FileReader(fname)
        );
        BufferedWriter writer=new BufferedWriter(
                new FileWriter(newfname)
        );
        String str;
        while ((str=reader.readLine())!=null) {
            // System.out.println(str);
            String encryptedText = crypto.encrypt(specName, key, ivParameterSpec, str);
            // System.out.println("cipherText: " + encryptedText);
            writer.write(encryptedText);
            writer.newLine();
        }
        reader.close();
        writer.close();
    }

    public static void File_decrypto(String fname, String newfname, String keyvalue) throws Exception {
        SecretKey key = new SecretKeySpec(keyvalue.getBytes(),"AES");
        IvParameterSpec ivParameterSpec = crypto.getIv(keyvalue);
        String specName = "AES/CBC/PKCS5Padding";
        BufferedReader reader=new BufferedReader(
                new FileReader(fname)
        );
        BufferedWriter writer=new BufferedWriter(
                new FileWriter(newfname)
        );
        String str;
        while ((str=reader.readLine())!=null) {
            //System.out.println(str);
            String decryptedText = crypto.decrypt(specName, key, ivParameterSpec, str);
            //System.out.println("decryptedText: " + decryptedText);
            writer.write(decryptedText);
            writer.newLine();
        }
        reader.close();
        writer.close();
    }
    public static int st_en(String[] args) throws Exception {
        String aesKey=Macro.getLocal("key");
        String origin=Macro.getLocal("origin");
        String target=Macro.getLocal("target");

        SFIToolkit.displayln("File Encryption");
        SFIToolkit.displayln("aeskey : "+aesKey);
        SFIToolkit.displayln("origin file : "+origin);
        SFIToolkit.displayln("target file : "+target);

        File_crypto(origin, target, aesKey);

        SFIToolkit.displayln("...done");
        return 0;
    }
    public static int st_de(String[] args) throws Exception {
        String aesKey=Macro.getLocal("key");
        String origin=Macro.getLocal("origin");
        String target=Macro.getLocal("target");

        SFIToolkit.displayln("File Encryption");
        SFIToolkit.displayln("aeskey : "+aesKey);
        SFIToolkit.displayln("origin file : "+origin);
        SFIToolkit.displayln("target file : "+target);

        File_decrypto(origin, target, aesKey);

        SFIToolkit.displayln("...done");
        return 0;
    }

    public static void main(String[] args) throws Exception {
        String aesKey="abcdefghigklmlolpoxqwxdftgyhujkh";
        String origin="/Users/seti/Desktop/test.ado";
        String target="/Users/seti/Desktop/test.cyado";
        String detarget="/Users/seti/Desktop/test.deado";

        File_crypto(origin,target,aesKey);
        File_decrypto(target,detarget,aesKey);
    }
}