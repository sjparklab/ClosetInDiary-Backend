package me.parkseongjong.springbootdeveloper.service;

import lombok.RequiredArgsConstructor;
import me.parkseongjong.springbootdeveloper.domain.RefreshToken;
import me.parkseongjong.springbootdeveloper.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken saveOrUpdate(RefreshToken refreshToken) {
        // 사용자 ID로 기존 리프레시 토큰이 존재하는지 확인
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserId(refreshToken.getUserId());

        if (existingToken.isPresent()) {
            // 기존 토큰이 있으면 업데이트
            RefreshToken token = existingToken.get();
            token.setRefreshToken(refreshToken.getRefreshToken());
            return refreshTokenRepository.save(token);
        } else {
            // 없으면 새로 저장
            return refreshTokenRepository.save(refreshToken);
        }
    }

    public RefreshToken findByRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Unexpected token"));
    }
}
