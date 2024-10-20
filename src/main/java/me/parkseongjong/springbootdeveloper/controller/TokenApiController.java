package me.parkseongjong.springbootdeveloper.controller;

import lombok.RequiredArgsConstructor;
import me.parkseongjong.springbootdeveloper.dto.CreateAccessTokenRequest;
import me.parkseongjong.springbootdeveloper.dto.CreateAccessTokenResponse;
import me.parkseongjong.springbootdeveloper.service.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/api")
@RequiredArgsConstructor
@RestController
public class TokenApiController {
    private final TokenService tokenService;

    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            // 'Bearer ' 접두사 제거
            if (authorizationHeader.startsWith("Bearer ")) {
                String refreshToken = authorizationHeader.substring(7);  // 'Bearer ' 제거 후 토큰 추출
                String newAccessToken = tokenService.createNewAccessToken(refreshToken);

                Map<String, String> response = new HashMap<>();
                response.put("accessToken", newAccessToken);

                return ResponseEntity.ok(response);
            } else {
                // 잘못된 형식의 헤더 처리
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }
}
