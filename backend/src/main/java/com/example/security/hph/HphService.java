package com.example.security.hph;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

record HphSeed(String seedId, long expiresAt) {}
record HphToken(String value, long expiresAt) {}

@Service
public class HphService {
  private final byte[] secretKey;
  private final Cache<String, Long> seedCache;
  private final Cache<String, Boolean> usedCache;

  public HphService(@Value("${hph.secret}") String secret) {
    this.secretKey = secret.getBytes(StandardCharsets.UTF_8);
    this.seedCache = Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).maximumSize(100000).build();
    this.usedCache = Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).maximumSize(100000).build();
  }

  public HphSeed issueSeed(String action) {
    String seedId = UUID.randomUUID().toString();
    long exp = Instant.now().plusSeconds(300).toEpochMilli();
    seedCache.put(seedId, exp);
    return new HphSeed(seedId, exp);
    // action can be remembered in a separate cache if you want to bind seed→action.
  }

  public HphToken activate(String seedId, String userAgent) {
    Long exp = seedCache.getIfPresent(seedId);
    if (exp == null || exp < System.currentTimeMillis()) {
      throw new IllegalStateException("Invalid or expired seed");
    }
    seedCache.invalidate(seedId);
    long expiresAt = Instant.now().plusSeconds(180).toEpochMilli();
    String payload = seedId + "|" + expiresAt + "|" + hashUA(userAgent);
    String sig = hmac(payload);
    String token = base64Url(payload) + "." + base64Url(sig);
    return new HphToken(token, expiresAt);
  }

  public boolean verify(String token, String userAgent) {
    try {
      String[] parts = token.split("\\.");
      if (parts.length != 2) return false;
      String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
      String sig = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
      if (!hmac(payload).equals(sig)) return false;

      String[] fields = payload.split("\\|");
      long exp = Long.parseLong(fields[1]);
      String uaHash = fields[2];
      if (exp < System.currentTimeMillis()) return false;
      if (!uaHash.equals(hashUA(userAgent == null ? "" : userAgent))) return false;

      if (usedCache.asMap().putIfAbsent(parts[0], Boolean.TRUE) != null) return false;
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private String hmac(String data) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secretKey, "HmacSHA256"));
      byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
      return new String(raw, StandardCharsets.ISO_8859_1);
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  private String hashUA(String ua) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secretKey, "HmacSHA256"));
      return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(ua.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) { return ""; }
  }

  private String base64Url(String s) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(s.getBytes(StandardCharsets.UTF_8));
  }
}
