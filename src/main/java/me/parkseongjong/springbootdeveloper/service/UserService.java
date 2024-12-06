package me.parkseongjong.springbootdeveloper.service;

import lombok.RequiredArgsConstructor;
import me.parkseongjong.springbootdeveloper.domain.User;
import me.parkseongjong.springbootdeveloper.dto.AddUserRequest;
import me.parkseongjong.springbootdeveloper.dto.LoginRequest;
import me.parkseongjong.springbootdeveloper.dto.SignupRequest;
import me.parkseongjong.springbootdeveloper.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public boolean isEmailAlreadyInUse(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public boolean authenticate(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElse(null);
        if (user == null) {
            return false;
        }
        return bCryptPasswordEncoder.matches(loginRequest.getPassword(), user.getPassword());
    }

    public Long save(SignupRequest dto) {
        return userRepository.save(User.builder()
                .username(dto.getUsername())
                .name(dto.getName())
                .email(dto.getEmail())
                .password(bCryptPasswordEncoder.encode(dto.getPassword()))
                .build()).getId();
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Unexpected user"));
    }

    // 이메일로 사용자 조회 메소드 추가
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public User putUser(User user) {
        return userRepository.save(user);
    }
}
