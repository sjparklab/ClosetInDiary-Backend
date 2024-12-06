package me.parkseongjong.springbootdeveloper.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.parkseongjong.springbootdeveloper.config.jwt.TokenProvider;
import me.parkseongjong.springbootdeveloper.domain.RefreshToken;
import me.parkseongjong.springbootdeveloper.domain.User;
import me.parkseongjong.springbootdeveloper.dto.LoginRequest;
import me.parkseongjong.springbootdeveloper.dto.SignupRequest;
import me.parkseongjong.springbootdeveloper.dto.UserDataResponse;
import me.parkseongjong.springbootdeveloper.dto.UserResponse;
import me.parkseongjong.springbootdeveloper.service.ImageService;
import me.parkseongjong.springbootdeveloper.service.RefreshTokenService;
import me.parkseongjong.springbootdeveloper.service.TokenService;
import me.parkseongjong.springbootdeveloper.service.UserService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.http.HttpHeaders;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class UserApiController {
    private final UserService userService;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final ImageService imageService;
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest loginRequest, HttpSession session) {
        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());

            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 액세스 토큰 생성
            User user = userService.findByEmail(loginRequest.getEmail());
            String accessToken = tokenProvider.generateToken(user, Duration.ofHours(2));

            // 리프레시 토큰 생성 및 저장 (중복 시 업데이트)
            String refreshToken = tokenProvider.generateToken(user, Duration.ofDays(14));
            refreshTokenService.saveOrUpdate(new RefreshToken(user.getId(), refreshToken));

            // 액세스 토큰 및 리프레시 토큰 반환
            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", accessToken);
            tokens.put("refreshToken", refreshToken);

            return ResponseEntity.ok(tokens);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequest signupRequest) {
        if (userService.isEmailAlreadyInUse(signupRequest.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already in use");
        }
        userService.save(signupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body("Signup successful");
    }

    @GetMapping("/user-data")
    public ResponseEntity<?> getUserInfo(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }

        return ResponseEntity.ok(new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getOnelineInfo(),
                null // 이미지 제외
        ));
    }

    @GetMapping("/user-profile-picture")
    public ResponseEntity<?> getProfilePicture(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }

        try {
            String fileKey = user.getProfilePicture();
            byte[] imageData = imageService.getImageFromS3(fileKey);

            return ResponseEntity.ok()
                    .header("Content-Type", "image/jpeg")
                    .body(imageData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to retrieve profile picture");
        }
    }

    @PutMapping("/user-data")
    public ResponseEntity<?> updateUserProfile(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "intro", required = false) String intro,
            @AuthenticationPrincipal User user) {

        if (user == null) {
            return new ResponseEntity<>("User not authenticated", HttpStatus.UNAUTHORIZED);
        }

        try {
            // 이미지 업로드
            if (file != null && !file.isEmpty()) {
                String imageUrl = imageService.uploadFileToS3(file, user.getId().toString());
                user.setProfilePicture(imageUrl);
            }

            // 다른 필드 업데이트
            if (name != null) {
                user.setName(name);
            }
            if (intro != null) {
                user.setOnelineInfo(intro);
            }

            userService.putUser(user);

            return ResponseEntity.ok("Profile updated successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update profile");
        }
    }
}