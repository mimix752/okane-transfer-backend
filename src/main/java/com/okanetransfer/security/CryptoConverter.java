package com.okanetransfer.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Convertisseur JPA — chiffrement AES-256 automatique des CINs en base (CDC 4.3)
 * Chiffre avant INSERT/UPDATE, déchiffre après SELECT
 */
@Component
@Converter
public class CryptoConverter implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    // Clé AES-256 : 32 caractères obligatoire (256 bits)
    // À définir dans application.properties : crypto.aes.secret=votre_cle_32_chars
    private static String staticSecret;

    @Value("${crypto.aes.secret}")
    public void setSecret(String secret) {
        if (secret.length() != 32) {
            throw new IllegalArgumentException(
                    "crypto.aes.secret doit faire exactement 32 caractères (AES-256)"
            );
        }
        CryptoConverter.staticSecret = secret;
    }

    // ─── Chiffrement (avant écriture en base) ───────────────────────────────────

    @Override
    public String convertToDatabaseColumn(String plainText) {
        if (plainText == null || plainText.isBlank()) return plainText;
        try {
            SecretKeySpec keySpec = new SecretKeySpec(staticSecret.getBytes(), "AES");

            // IV fixe dérivé de la clé (16 premiers octets) — simple et reproductible
            byte[] iv = staticSecret.substring(0, 16).getBytes();
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encrypted);

        } catch (Exception e) {
            throw new RuntimeException("Erreur chiffrement CIN", e);
        }
    }

    // ─── Déchiffrement (après lecture depuis base) ───────────────────────────────

    @Override
    public String convertToEntityAttribute(String encryptedText) {
        if (encryptedText == null || encryptedText.isBlank()) return encryptedText;
        try {
            SecretKeySpec keySpec = new SecretKeySpec(staticSecret.getBytes(), "AES");

            byte[] iv = staticSecret.substring(0, 16).getBytes();
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decrypted, "UTF-8");

        } catch (Exception e) {
            throw new RuntimeException("Erreur déchiffrement CIN", e);
        }
    }
}