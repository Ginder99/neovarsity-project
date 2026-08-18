package com.vms.order.security.internal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
public class HmacVerifier {

    private static final String HMAC_ALGO = "HmacSHA256";

    private final String serviceSecret;

    public HmacVerifier(
            @Value("${internal-security.service-secret}") String serviceSecret) {
        this.serviceSecret = serviceSecret;
    }

    public String buildCanonicalString(String httpMethod, String path, String timestamp, String body) {
        return httpMethod.toUpperCase() + "\n" + path + "\n" + timestamp + "\n" + body;
    }

    private String computeSignature(String canonicalString) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(serviceSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGO));
            byte[] rawHmac = mac.doFinal(canonicalString.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(rawHmac);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute HMAC signature", e);
        }
    }

    public boolean isValid(String httpMethod, String path, String timestamp, String body, String providedSignature) {
        if (timestamp == null || providedSignature == null) {
            return false;
        }
        String rawPayload =  httpMethod.toUpperCase() + "\n" + path + "\n" + timestamp + "\n" + body;
        String expectedSignature = computeSignature(rawPayload);

        return MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                providedSignature.getBytes(StandardCharsets.UTF_8));
    }
}
