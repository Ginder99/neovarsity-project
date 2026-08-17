package com.vms.order.service;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Service
public class QrPayloadService {

    private final ObjectMapper objectMapper;
    private final SecretKeySpec hmacKey;

    public QrPayloadService(ObjectMapper objectMapper, @Value("${jwt.secret}") String secret) {
        this.objectMapper = objectMapper;
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (Exception ex) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        if (keyBytes.length < 32) {
            keyBytes = Keys.hmacShaKeyFor(secret.repeat(Math.max(1, 32 / Math.max(1, secret.length()) + 1)).getBytes(StandardCharsets.UTF_8)).getEncoded();
        }
        this.hmacKey = new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    public String generatePayload(String orderId, Instant expiresAt) {
        try {
            String data = orderId + ":" + expiresAt.getEpochSecond();
            String hmac = Base64.getUrlEncoder().withoutPadding().encodeToString(sign(data));
            Map<String, Object> payload = Map.of(
                    "order_id", orderId,
                    "exp", expiresAt.getEpochSecond(),
                    "hmac", hmac
            );
            return Base64.getUrlEncoder().withoutPadding().encodeToString(objectMapper.writeValueAsBytes(payload));
        } catch (Exception ex) {
            throw new ApiException(500, "QR_GENERATION_ERROR", "Unable to generate QR payload");
        }
    }

    public DecodedPayload decodeAndValidate(String payload) {
        try {
            byte[] raw = Base64.getUrlDecoder().decode(payload);
            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = objectMapper.readValue(raw, Map.class);
            String orderId = String.valueOf(parsed.get("order_id"));
            long exp = Long.parseLong(String.valueOf(parsed.get("exp")));
            String hmac = String.valueOf(parsed.get("hmac"));
            String expected = Base64.getUrlEncoder().withoutPadding().encodeToString(sign(orderId + ":" + exp));
            if (!expected.equals(hmac)) {
                throw new ApiException(400, "INVALID_QR", "Invalid QR payload signature");
            }
            return new DecodedPayload(orderId, Instant.ofEpochSecond(exp));
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException(400, "INVALID_QR", "Invalid QR payload");
        }
    }

    private byte[] sign(String value) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(hmacKey);
        return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
    }

    public record DecodedPayload(String orderId, Instant expiresAt) {
    }
}
