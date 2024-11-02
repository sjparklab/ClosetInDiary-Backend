package me.parkseongjong.springbootdeveloper.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.parkseongjong.springbootdeveloper.domain.Diary;
import me.parkseongjong.springbootdeveloper.domain.Outfit;
import me.parkseongjong.springbootdeveloper.domain.User;
import me.parkseongjong.springbootdeveloper.dto.DiaryRequest;
import me.parkseongjong.springbootdeveloper.repository.DiaryRepository;
import me.parkseongjong.springbootdeveloper.repository.OutfitRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final OutfitRepository outfitRepository;

    public List<Diary> findAllDiaries() {
        return diaryRepository.findAll();
    }

    public List<Diary> findDiariesByUserId(Long userId) {
        return diaryRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("not found: " + userId));
    }

    public Diary findDiaryById(Long id) {
        return diaryRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Diary not found"));
    }

    @Transactional
    public Diary createDiary(DiaryRequest diaryRequest) {
        Diary diary = Diary.builder()
                .date(diaryRequest.getDate())
                .user(diaryRequest.getUser())
                .emotion(diaryRequest.getEmotion())
                .content(diaryRequest.getContent())
                .build();

        // outfitIds를 통해 Outfit 객체 목록을 가져와서 다이어리에 추가
        List<Outfit> outfits = diaryRequest.getOutfitIds().stream()
                .map(outfitRepository::findById)
                .flatMap(optionalOutfit -> optionalOutfit.stream()) // Optional이 값을 가진 경우에만 스트림으로 변환
                .collect(Collectors.toList());

        diary.setOutfits(outfits);

        return diaryRepository.save(diary);
    }
}
