package me.parkseongjong.springbootdeveloper.service;

import lombok.RequiredArgsConstructor;
import me.parkseongjong.springbootdeveloper.domain.Diary;
import me.parkseongjong.springbootdeveloper.domain.User;
import me.parkseongjong.springbootdeveloper.repository.DiaryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;

    public List<Diary> findAllDiaries() {
        return diaryRepository.findAll();
    }

    public List<Diary> findDiariesByUserId(Long userId) {
        return diaryRepository.findByUserId(userId);
    }

    public Diary findDiaryById(Long id) {
        return diaryRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Diary not found"));
    }

    public Diary saveDiary(Diary diary) {
        return diaryRepository.save(diary);
    }
}
