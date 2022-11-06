package com.tsid.auth.util;

import com.tsid.auth.exception.AuthServerException;
import com.tsid.auth.exception.ErrCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Slf4j
@Component
public class EncryptUtil {

    private static String RSA_PRIVATE_KEY;

    @Value("${rsa.private.key}")
    public void setRsaPrivateKey(String key){
        this.RSA_PRIVATE_KEY = key;
    }

    public static String encrypt(String text) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.update(text.getBytes());

        return bytesToHex(md.digest());
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    /**
     * RSA 복호화
     */
    public static String rsaDecrpyt(String data){
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] bytePrivateKey = Base64.getDecoder().decode(RSA_PRIVATE_KEY.getBytes());
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(bytePrivateKey);
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] byteEncryptedData = Base64.getDecoder().decode(data.getBytes("UTF-8"));
            byte[] byteDecryptedData = cipher.doFinal(byteEncryptedData);

            return new String(byteDecryptedData);
        } catch (Exception e) {
            throw new AuthServerException(ErrCode.INTERNAL_ERROR, "RSA 복호화 오류");
        }
    }

    public static void rsaKeyPair(){
        try {
            SecureRandom secureRandom = new SecureRandom();
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048, secureRandom);
            KeyPair keyPair = keyPairGenerator.genKeyPair();

            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            String stringPublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            String stringprivateKey = Base64.getEncoder().encodeToString(privateKey.getEncoded());

            log.info("stringPublicKey ::: " + stringPublicKey);
            log.info("stringprivateKey ::: " + stringprivateKey);

        } catch (Exception e) {
            throw new AuthServerException(ErrCode.INTERNAL_ERROR, "RSA keypair 오류");
        }
    }

    public static String aes256Encrypt(String key, String data) {
        String alg = "AES/CBC/PKCS5Padding";

        String iv = key.substring(0, 16); //16byte

        try {
            Cipher cipher = Cipher.getInstance(alg);

            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivParamSpec = new IvParameterSpec(iv.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParamSpec);

            byte[] encrypted = cipher.doFinal(data.getBytes("UTF-8"));

            return Base64.getUrlEncoder().encodeToString(encrypted);
//            return Base64.getEncoder().encodeToString(encrypted1);

        } catch (Exception e) {
            throw new AuthServerException(ErrCode.INTERNAL_ERROR, "암호화 오류");
        }
    }

    public static String aes256Decrypt(String key, String data) {
        String alg = "AES/CBC/PKCS5Padding";

        String iv = key.substring(0, 16); //16byte

        try {
            Cipher cipher = Cipher.getInstance(alg);
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");

            IvParameterSpec ivParamSpec = new IvParameterSpec(iv.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParamSpec);

            byte[] decodedBytes = Base64.getUrlDecoder().decode(data);
//            byte[] decodedBytes = Base64.getDecoder().decode(data);
            byte[] decrypted = cipher.doFinal(decodedBytes);

            return new String(decrypted, "UTF-8");
        }catch (Exception e) {
            log.error(e.getMessage());
            throw new AuthServerException(ErrCode.INTERNAL_ERROR, "aes256Decrypt 복호화 오류");
        }
    }

}