package com.example.security.recaptcha;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class RecaptchaService {
  private final WebClient client = WebClient.create("https://www.google.com/recaptcha/api/siteverify");
  @Value("${recaptcha.secret}") String secret;

  public RecaptchaResult verify(String token, String expectedAction) {
    Map<String, Object> res = client.post()
      .contentType(MediaType.APPLICATION_FORM_URLENCODED)
      .bodyValue("secret=" + secret + "&response=" + token)
      .retrieve().bodyToMono(Map.class).block();

    boolean success = Boolean.TRUE.equals(res.get("success"));
    double score = success && res.get("score") != null ? ((Number)res.get("score")).doubleValue() : 0.0;
    String action = res.get("action") != null ? res.get("action").toString() : "";
    return new RecaptchaResult(success, score, action);
  }

  public record RecaptchaResult(boolean success, double score, String action) {}
}
