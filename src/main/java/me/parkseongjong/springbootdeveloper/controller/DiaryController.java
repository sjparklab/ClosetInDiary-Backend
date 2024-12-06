package me.parkseongjong.springbootdeveloper.controller;

import lombok.RequiredArgsConstructor;
import me.parkseongjong.springbootdeveloper.domain.Diary;
import me.parkseongjong.springbootdeveloper.domain.User;
import me.parkseongjong.springbootdeveloper.dto.DiaryRequest;
import me.parkseongjong.springbootdeveloper.dto.UpdateDiaryRequest;
import me.parkseongjong.springbootdeveloper.service.DiaryService;
import me.parkseongjong.springbootdeveloper.service.ImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;
    private final ImageService imageService;
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
            @RequestPart(value = "mainImage", required = false) MultipartFile mainImage,
            @RequestPart(value = "subImages", required = false) List<MultipartFile> subImages
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        diaryRequest.setUser(user);

        String mainImagePath = null;
        if (mainImage != null && !mainImage.isEmpty()) {
            // ImageService를 통해 S3 업로드
            mainImagePath = imageService.uploadFileToS3(mainImage, user.getId().toString());
        }

        List<String> subImagePaths = null;
        if (subImages != null && !subImages.isEmpty()) {
            subImagePaths = subImages.stream()
                    .map(file -> imageService.uploadFileToS3(file, user.getId().toString()))
                    .toList();
        }

        diaryRequest.setMainImagePath(mainImagePath);
        diaryRequest.setSubImagePaths(subImagePaths);

        Diary createdDiary = diaryService.createDiary(diaryRequest);
        return ResponseEntity.ok(createdDiary);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Diary> modifyDiary(@AuthenticationPrincipal User user, @PathVariable Long id, @RequestBody UpdateDiaryRequest updateDiaryRequest) {
        Diary diary = diaryService.findDiaryById(id);

        if (diary == null || !diary.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 다이어리가 없거나 접근 권한이 없으면 예외 처리
        }

        updateDiaryRequest.setId(id);
        updateDiaryRequest.setUser(user); // 로그인된 사용자 정보를 다이어리에 설정
        Diary updatedDiary = diaryService.updateDiary(updateDiaryRequest);
        return ResponseEntity.ok(updatedDiary);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDiary(@AuthenticationPrincipal User user, @PathVariable Long id) {
        Diary diary = diaryService.findDiaryById(id);

        if (diary == null || !diary.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 다이어리가 없거나 접근 권한이 없으면 예외 처리
        }

        diaryService.deleteDiary(id);
        return ResponseEntity.ok("id " + id + ": Diary Deleted Complete!");
    }
}
