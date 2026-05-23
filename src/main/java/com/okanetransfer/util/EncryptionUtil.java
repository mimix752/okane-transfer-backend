package com.okanetransfer.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class EncryptionUtil {

    private static final String SECRET_KEY_ENV = "AES_SECRET_KEY";
    private static final String ALGORITHM      = "AES/GCM/NoPadding";
    private static final int    GCM_IV_LENGTH  = 12;
    private static final int    GCM_TAG_LENGTH = 128;


    public static String encrypt(String plainText) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    getSecretKey(),
                    new GCMParameterSpec(GCM_TAG_LENGTH, iv)
            );

            byte[] encrypted = cipher.doFinal(
                    plainText.getBytes("UTF-8")
            );


            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv,        0, combined, 0,         iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }



    public static String decrypt(String encryptedText) {
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedText);


            byte[] iv        = new byte[GCM_IV_LENGTH];
            byte[] encrypted = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, 0,             iv,        0, iv.length);
            System.arraycopy(combined, GCM_IV_LENGTH, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    getSecretKey(),
                    new GCMParameterSpec(GCM_TAG_LENGTH, iv)
            );

            return new String(cipher.doFinal(encrypted), "UTF-8");

        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }



    private static SecretKey getSecretKey() {

        String keyBase64 = System.getProperty(
                SECRET_KEY_ENV,
                System.getenv(SECRET_KEY_ENV)
        );
        if (keyBase64 == null) {
            throw new RuntimeException(
                    "AES_SECRET_KEY not configured"
            );
        }
        byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
        return new SecretKeySpec(keyBytes, "AES");
    }
}