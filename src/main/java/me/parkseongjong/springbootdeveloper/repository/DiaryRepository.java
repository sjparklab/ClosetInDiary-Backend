package me.parkseongjong.springbootdeveloper.repository;

import me.parkseongjong.springbootdeveloper.domain.Diary;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    Optional<List<Diary>> findByUserId(Long userId, Sort sort);
    Optional<List<Diary>> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate, Sort sort);
}
