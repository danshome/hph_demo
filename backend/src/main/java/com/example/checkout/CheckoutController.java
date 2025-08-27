package com.example.checkout;

import com.example.security.hph.HphService;
import com.example.security.recaptcha.RecaptchaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {
  private final HphService hphService;
  private final RecaptchaService rcService;

  @PostMapping("/start")
  public ResponseEntity<?> start(@RequestBody Map<String, String> body,
                                 @RequestHeader(name = "X-HPH", required = false) String hphToken,
                                 HttpServletRequest req) {
    String email = body.getOrDefault("email", "");
    String action = body.getOrDefault("action", "checkout");
    String rcToken = body.get("recaptchaToken");
    if (email.isBlank() || rcToken == null) {
      return ResponseEntity.badRequest().body(Map.of("message","Missing fields"));
    }

    var rc = rcService.verify(rcToken, action);
    double threshold = "checkout".equals(action) ? 0.85 : 0.6;
    if (!rc.success() || rc.score() < threshold || !action.equals(rc.action())) {
      return ResponseEntity.status(403).body(Map.of("message","Failed reCAPTCHA verification"));
    }

    String ua = req.getHeader("User-Agent");
    if (hphToken == null || !hphService.verify(hphToken, ua)) {
      return ResponseEntity.status(403).body(Map.of("message","Human presence verification failed"));
    }

    String redirectUrl = "https://payment.example/redirect?order=123";
    return ResponseEntity.ok(Map.of("redirectUrl", redirectUrl));
  }
}
