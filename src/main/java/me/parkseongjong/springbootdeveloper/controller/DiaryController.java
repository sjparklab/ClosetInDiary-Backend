package me.parkseongjong.springbootdeveloper.controller;

import lombok.RequiredArgsConstructor;
import me.parkseongjong.springbootdeveloper.domain.Diary;
import me.parkseongjong.springbootdeveloper.domain.Outfit;
import me.parkseongjong.springbootdeveloper.domain.User;
import me.parkseongjong.springbootdeveloper.dto.DiaryRequest;
import me.parkseongjong.springbootdeveloper.dto.UpdateDiaryRequest;
import me.parkseongjong.springbootdeveloper.repository.DiaryRepository;
import me.parkseongjong.springbootdeveloper.service.DiaryService;
import me.parkseongjong.springbootdeveloper.service.ImageService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;
    private final ImageService imageService;
    private final DiaryRepository diaryRepository;
    private static final String BUCKET_NAME = "closetindiary-image-bucket";

    // 특정 유저의 다이어리 조회 (로그인된 사용자만)
    @GetMapping
    public ResponseEntity<List<Diary>> getMyDiaries(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "latest") String sort) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Diary> diaries = diaryService.findDiariesByUserIdAndDateRange(user.getId(), startDate, endDate, sort);
        return ResponseEntity.ok(diaries);
    }

    // 특정 다이어리 조회
    @GetMapping("/{id}")
    public ResponseEntity<Diary> getDiary(@PathVariable Long id, @AuthenticationPrincipal User user) {
        Diary diary = diaryService.findDiaryById(id);

        if (diary == null || !diary.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 다이어리가 없거나 접근 권한이 없으면 예외 처리
        }

        return ResponseEntity.ok(diary);
    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<Diary> createDiary(
            @AuthenticationPrincipal User user,
            @RequestPart("data") DiaryRequest diaryRequest,
            @RequestPart(value = "mainImage", required = false) MultipartFile mainImage
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        diaryRequest.setUser(user);

        String mainImagePath = null;
        if (mainImage != null && !mainImage.isEmpty()) {
            mainImagePath = imageService.uploadFileToS3(mainImage, user.getId().toString());
        }

        diaryRequest.setMainImagePath(mainImagePath);

        Diary createdDiary = diaryService.createDiary(diaryRequest);
        return ResponseEntity.ok(createdDiary);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Diary> modifyDiary(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestPart("data") UpdateDiaryRequest updateDiaryRequest,
            @RequestPart(value = "mainImage", required = false) MultipartFile mainImage) {

        try {
            Diary diary = diaryService.findDiaryById(id);

            if (diary == null || !diary.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            updateDiaryRequest.setId(id);
            updateDiaryRequest.setUser(user);

            String mainImagePath = null;
            if (mainImage != null && !mainImage.isEmpty()) {
                mainImagePath = imageService.uploadFileToS3(mainImage, user.getId().toString());
            }

            updateDiaryRequest.setMainImagePath(mainImagePath);

            Diary updatedDiary = diaryService.updateDiary(updateDiaryRequest);
            return ResponseEntity.ok(updatedDiary);
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<
            String> deleteDiary(@AuthenticationPrincipal User user, @PathVariable Long id) {
        Diary diary = diaryService.findDiaryById(id);

        if (diary == null || !diary.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 다이어리가 없거나 접근 권한이 없으면 예외 처리
        }

        diaryService.deleteDiary(id);
        return ResponseEntity.ok("id " + id + ": Diary Deleted Complete!");
    }

    @GetMapping("/image/{fileKey}")
    public ResponseEntity<?> getProfilePicture(@PathVariable String fileKey, @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }

        if (fileKey == null || fileKey.isEmpty() || fileKey.equals("undefined") || fileKey.equals("null")) {
            return ResponseEntity.status(400).body("Invalid file key");
        }

        try {
            byte[] imageData = imageService.getImageFromS3(fileKey);

            return ResponseEntity.ok()
                    .header("Content-Type", "image/jpeg")
                    .body(imageData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to retrieve profile picture");
        }
    }
}
