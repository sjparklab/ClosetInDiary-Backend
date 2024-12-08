package me.parkseongjong.springbootdeveloper.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.parkseongjong.springbootdeveloper.domain.Diary;
import me.parkseongjong.springbootdeveloper.dto.DiaryRequest;
import me.parkseongjong.springbootdeveloper.dto.UpdateDiaryRequest;
import me.parkseongjong.springbootdeveloper.repository.DiaryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;

    public List<Diary> findAllDiaries() {
        return diaryRepository.findAll();
    }

    public List<Diary> findDiariesByUserId(Long userId) {
        return diaryRepository.findByUserId(userId, Sort.by(Sort.Direction.DESC, "date"))
                .orElseThrow(() -> new IllegalArgumentException("not found: " + userId));
    }

    public Diary findDiaryById(Long id) {
        return diaryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Diary not found"));
    }

    public List<Diary> findDiariesByUserIdAndDateRange(Long userId, String startDateStr, String endDateStr, String sort) {
        LocalDate startDate = null;
        LocalDate endDate = null;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try {
            if (startDateStr != null) {
                startDate = LocalDate.parse(startDateStr, formatter);
            }
            if (endDateStr != null) {
                endDate = LocalDate.parse(endDateStr, formatter);
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Please use yyyy-MM-dd.");
        }

        // 날짜 범위 검증
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date.");
        }

        Sort sortOption = Sort.by(Sort.Direction.DESC, "date");
        if ("oldest".equalsIgnoreCase(sort)) {
            sortOption = Sort.by(Sort.Direction.ASC, "date");
        }

        if (startDate != null && endDate != null) {
            return diaryRepository.findByUserIdAndDateBetween(userId, startDate, endDate, sortOption)
                    .orElseThrow(() -> new IllegalArgumentException("not found: " + userId));
        } else {
            return diaryRepository.findByUserId(userId, sortOption)
                    .orElseThrow(() -> new IllegalArgumentException("not found: " + userId));
        }
    }

    @Transactional
    public Diary createDiary(DiaryRequest diaryRequest) {
        // date 문자열 -> LocalDate 변환
        LocalDate parsedDate = LocalDate.parse(diaryRequest.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        Diary diary = Diary.builder()
                .date(parsedDate)
                .user(diaryRequest.getUser())
                .title(diaryRequest.getTitle())
                .content(diaryRequest.getContent())
                .mainImagePath(diaryRequest.getMainImagePath())
                .subImagePaths(diaryRequest.getSubImagePaths())
                .build();

        // outfits 관련 로직 제거

        return diaryRepository.save(diary);
    }

    @Transactional
    public Diary updateDiary(UpdateDiaryRequest updateDiaryRequest) {
        Diary diary = diaryRepository.findById(updateDiaryRequest.getId())
                .orElseThrow(() -> new IllegalArgumentException("Diary not found"));

        diary.setTitle(updateDiaryRequest.getTitle());
        diary.setContent(updateDiaryRequest.getContent());
        diary.setMainImagePath(updateDiaryRequest.getMainImagePath());
        diary.setSubImagePaths(new ArrayList<>(updateDiaryRequest.getSubImagePaths()));

        diaryRepository.save(diary);
        return diary;
    }
    @Transactional
    public void deleteDiary(Long id) {
        Diary diary = diaryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Diary not found with id: " + id));

        diaryRepository.delete(diary);
    }
}
