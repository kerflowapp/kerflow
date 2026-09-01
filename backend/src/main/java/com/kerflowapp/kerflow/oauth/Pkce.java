package com.kerflowapp.kerflow.oauth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * PKCE S256 verification (RFC 7636). Only S256 is accepted: "plain" offers no protection
 * against code interception, and OAuth 2.1 drops it.
 */
public final class Pkce {

    private Pkce() {
    }

    public static boolean verify(String codeVerifier, String codeChallenge) {
        if (codeVerifier == null || codeChallenge == null) {
            return false;
        }
        return MessageDigest.isEqual(
            challengeOf(codeVerifier).getBytes(StandardCharsets.UTF_8),
            codeChallenge.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * BASE64URL-ENCODE(SHA256(ASCII(code_verifier))), unpadded.
     */
    private static String challengeOf(String codeVerifier) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

}
