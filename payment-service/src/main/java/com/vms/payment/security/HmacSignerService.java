package com.vms.payment.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@Component
public class HmacSignerService {

    private static final String HMAC_ALGO = "HmacSHA256";

    private final String serviceSecret;
    private final String serviceName;

    public HmacSignerService(
            @Value("${internal-security.service-secret}") String serviceSecret,
            @Value("${internal-security.service-name}") String serviceName) {
        this.serviceSecret = serviceSecret;
        this.serviceName = serviceName;
    }

    public String buildCanonicalString(String httpMethod, String path, String timestamp, String body) {
        return httpMethod.toUpperCase() + "\n" + path + "\n" + timestamp + "\n" + body;
    }

    public String sign(String canonicalString) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(serviceSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGO));
            byte[] rawHmac = mac.doFinal(canonicalString.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(rawHmac);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute HMAC signature", e);
        }
    }

    public String serviceName() {
        return serviceName;
    }
}
