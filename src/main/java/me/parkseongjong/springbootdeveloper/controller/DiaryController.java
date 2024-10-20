package me.parkseongjong.springbootdeveloper.controller;

import lombok.RequiredArgsConstructor;
import me.parkseongjong.springbootdeveloper.domain.Diary;
import me.parkseongjong.springbootdeveloper.domain.User;
import me.parkseongjong.springbootdeveloper.service.DiaryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    // 모든 다이어리 조회
    @GetMapping
    public ResponseEntity<List<Diary>> getAllDiaries() {
        List<Diary> diaries = diaryService.findAllDiaries();
        return ResponseEntity.ok(diaries);
    }

    // 특정 유저의 다이어리 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Diary>> getDiariesByUser(@PathVariable Long userId) {
        List<Diary> diaries = diaryService.findDiariesByUserId(userId);
        return ResponseEntity.ok(diaries);
    }

    // 특정 다이어리 조회
    @GetMapping("/{id}")
    public ResponseEntity<Diary> getDiary(@PathVariable Long id) {
        Diary diary = diaryService.findDiaryById(id);
        return ResponseEntity.ok(diary);
    }

    // 다이어리 생성
    @PostMapping
    public ResponseEntity<Diary> createDiary(@AuthenticationPrincipal User user, @RequestBody Diary diary) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 유저 정보가 없으면 예외 처리
        }

        diary.setUser(user); // 로그인된 사용자 정보를 다이어리에 설정
        Diary createdDiary = diaryService.saveDiary(diary);
        return ResponseEntity.ok(createdDiary);
    }
}
