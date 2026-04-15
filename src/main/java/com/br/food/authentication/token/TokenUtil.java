package com.br.food.authentication.token;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class TokenUtil {
    private static final SecureRandom RNG = new SecureRandom();

    private TokenUtil() {
    }

    /** Gera o refresh token opaco (valor em claro que vai para o cookie) */
    public static String generateOpaqueToken() {
        byte[] bytes = new byte[64]; // 512 bits
        RNG.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** Hash do refresh para persistir com seguranÃ§a (nÃ£o salvar o raw no banco) */
    public static String sha256(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] out = md.digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

