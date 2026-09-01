package com.kerflowapp.kerflow.oauth;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PkceTest {

    // Test vector from RFC 7636 appendix B.
    private static final String RFC_VERIFIER = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";
    private static final String RFC_CHALLENGE = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM";

    @Test
    void accepts_the_rfc_7636_reference_vector() {
        assertThat(Pkce.verify(RFC_VERIFIER, RFC_CHALLENGE)).isTrue();
    }

    @Test
    void rejects_a_verifier_that_does_not_match_the_challenge() {
        assertThat(Pkce.verify("some-other-verifier", RFC_CHALLENGE)).isFalse();
    }

    @Test
    void rejects_a_verifier_presented_as_its_own_challenge() {
        // Guards against accidentally accepting the "plain" method, which OAuth 2.1 drops.
        assertThat(Pkce.verify(RFC_VERIFIER, RFC_VERIFIER)).isFalse();
    }

    @Test
    void rejects_null_input() {
        assertThat(Pkce.verify(null, RFC_CHALLENGE)).isFalse();
        assertThat(Pkce.verify(RFC_VERIFIER, null)).isFalse();
    }
}
