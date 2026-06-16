package com.okanetransfer.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Convertisseur JPA — chiffrement AES-256-CBC automatique des CINs en base.
 * IV aléatoire de 16 octets généré à chaque chiffrement, stocké en préfixe du ciphertext.
 */
@Component
@Converter
public class CryptoConverter implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static String staticSecret;

    @Value("${crypto.aes.secret}")
    public void setSecret(String secret) {
        if (secret.length() != 32)
            throw new IllegalArgumentException("crypto.aes.secret doit faire exactement 32 caractères (AES-256)");
        CryptoConverter.staticSecret = secret;
    }

    @Override
    public String convertToDatabaseColumn(String plainText) {
        if (plainText == null || plainText.isBlank()) return plainText;
        try {
            byte[] iv = new byte[IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);

            SecretKeySpec keySpec = new SecretKeySpec(staticSecret.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));

            byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));

            // Préfixer l'IV au ciphertext : IV(16) + ciphertext
            byte[] combined = new byte[IV_LENGTH + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
            System.arraycopy(encrypted, 0, combined, IV_LENGTH, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Erreur chiffrement CIN", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String encryptedText) {
        if (encryptedText == null || encryptedText.isBlank()) return encryptedText;
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedText);

            // Extraire IV (16 premiers octets) et ciphertext
            byte[] iv = new byte[IV_LENGTH];
            byte[] ciphertext = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
            System.arraycopy(combined, IV_LENGTH, ciphertext, 0, ciphertext.length);

            SecretKeySpec keySpec = new SecretKeySpec(staticSecret.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));

            return new String(cipher.doFinal(ciphertext), "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Erreur déchiffrement CIN", e);
        }
    }
}
