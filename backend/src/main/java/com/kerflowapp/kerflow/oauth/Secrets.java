package com.kerflowapp.kerflow.oauth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Random secret generation and hashing for every credential the OAuth layer hands out
 * (client secrets, authorization codes, refresh tokens) and for personal API tokens.
 * Secrets are never stored in clear: only the SHA-256 hex digest is persisted.
 */
public final class Secrets {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int DEFAULT_RANDOM_BYTES = 32;

    private Secrets() {
    }

    public static String random() {
        return random(DEFAULT_RANDOM_BYTES);
    }

    public static String random(int bytes) {
        byte[] randomBytes = new byte[bytes];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Constant-time comparison, for secrets compared outside of a database unique-index lookup.
     */
    public static boolean constantTimeEquals(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        return MessageDigest.isEqual(left.getBytes(StandardCharsets.UTF_8), right.getBytes(StandardCharsets.UTF_8));
    }

}
