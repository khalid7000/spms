package com.rit.spms.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Covers the required test matrix from the integration contract's ยง6.9 acceptance
 * checklist for the JWT-verification layer itself: valid token, expiry, wrong issuer, and
 * unknown kid. Revoked jti and unprovisioned-user (403) are covered separately in
 * {@code GatewaySsoAuthenticationFilterTest}, since those are the filter's own concerns, not
 * the decoder's. Uses a real, locally-generated RSA keypair and a real embedded HTTP server
 * serving the JWK Set -- never the live gateway's private key, which this app never has
 * access to in the first place.
 */
class GatewaySsoJwtDecoderConfigTest {

    private static final String ISSUER = "academicspring-auth";
    private static final String KEY_ID = "test-key-1";

    private static HttpServer server;
    private static String jwksUri;
    private static RSAPrivateKey privateKey;
    private static JwtDecoder decoder;

    @BeforeAll
    static void setUp() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        privateKey = (RSAPrivateKey) keyPair.getPrivate();

        RSAKey jwk = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .keyID(KEY_ID)
                .build();
        String jwkSetJson = new com.nimbusds.jose.jwk.JWKSet(jwk).toPublicJWKSet().toString();

        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/.well-known/jwks.json", exchange -> {
            byte[] body = jwkSetJson.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        jwksUri = "http://localhost:" + server.getAddress().getPort() + "/.well-known/jwks.json";

        GatewaySsoProperties properties = new GatewaySsoProperties();
        properties.setJwksUri(jwksUri);
        properties.setIssuer(ISSUER);
        properties.setClockSkewSeconds(0);
        decoder = new GatewaySsoJwtDecoderConfig(properties).gatewaySsoJwtDecoder();
    }

    @AfterAll
    static void tearDown() {
        server.stop(0);
    }

    private String sign(String issuer, String keyId, Instant expiry, String jti) throws JOSEException {
        JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder()
                .subject("abc1234")
                .issuer(issuer)
                .claim("uid", "abc1234")
                .claim("email", "abc1234@g.rit.edu")
                .issueTime(new Date())
                .expirationTime(Date.from(expiry));
        if (jti != null) {
            claims.jwtID(jti);
        }
        SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(keyId).build(), claims.build());
        jwt.sign(new RSASSASigner(privateKey));
        return jwt.serialize();
    }

    @Test
    void validToken_decodesSuccessfully() throws Exception {
        String token = sign(ISSUER, KEY_ID, Instant.now().plusSeconds(300), UUID.randomUUID().toString());

        Jwt jwt = decoder.decode(token);

        assertThat(jwt.getClaimAsString("email")).isEqualTo("abc1234@g.rit.edu");
        assertThat(jwt.getId()).isNotBlank();
    }

    @Test
    void expiredToken_isRejected() throws Exception {
        String token = sign(ISSUER, KEY_ID, Instant.now().minusSeconds(60), UUID.randomUUID().toString());

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void wrongIssuer_isRejected() throws Exception {
        String token = sign("some-other-issuer", KEY_ID, Instant.now().plusSeconds(300), UUID.randomUUID().toString());

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void unknownKid_isRejected() throws Exception {
        String token = sign(ISSUER, "some-unregistered-kid", Instant.now().plusSeconds(300), UUID.randomUUID().toString());

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void missingJti_isRejected() throws Exception {
        String token = sign(ISSUER, KEY_ID, Instant.now().plusSeconds(300), null);

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }
}
