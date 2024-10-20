package me.parkseongjong.springbootdeveloper.service;

import lombok.RequiredArgsConstructor;
import me.parkseongjong.springbootdeveloper.config.jwt.TokenProvider;
import me.parkseongjong.springbootdeveloper.domain.User;
import org.springframework.stereotype.Service;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class TokenService {
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    public String createNewAccessToken(String refreshToken) {
        if (refreshToken.startsWith("Bearer ")) {
            refreshToken = refreshToken.substring(7);  // 'Bearer ' 제거
        }

        if (!tokenProvider.validToken(refreshToken)) {
            System.out.println("VAILD ERROR");
            throw new IllegalArgumentException("Unexpected token");
        }

        Long userId = refreshTokenService.findByRefreshToken(refreshToken).getUserId();
        User user = userService.findById(userId);

        return tokenProvider.generateToken(user, Duration.ofHours(2));
    }
}