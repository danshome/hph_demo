package com.example.security.hph;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/hph")
@RequiredArgsConstructor
public class HPHController {
  private final HphService hph;

  @PostMapping("/seed")
  public ResponseEntity<?> seed(@RequestBody Map<String, String> body) {
    String action = body.getOrDefault("action", "default");
    HphSeed seed = hph.issueSeed(action);
    return ResponseEntity.ok(Map.of("seedId", seed.seedId(), "expiresAt", seed.expiresAt()));
  }

  @PostMapping("/activate")
  public ResponseEntity<?> activate(@RequestBody Map<String, String> body,
                                    @RequestHeader(value = "User-Agent", required = false) String ua) {
    String seedId = body.get("seedId");
    if (seedId == null) return ResponseEntity.badRequest().body(Map.of("message","Missing seedId"));
    HphToken token = hph.activate(seedId, ua == null ? "" : ua);
    return ResponseEntity.ok(Map.of("token", token.value(), "expiresAt", token.expiresAt()));
  }
}
