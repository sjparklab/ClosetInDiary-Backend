package me.parkseongjong.springbootdeveloper.repository;

import me.parkseongjong.springbootdeveloper.domain.Outfit;
import me.parkseongjong.springbootdeveloper.domain.OutfitCategory;
import me.parkseongjong.springbootdeveloper.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OutfitRepository extends JpaRepository<Outfit, Long> {
    Optional<List<Outfit>> findAllByUserId(Long userId);
    Optional<Outfit> findByFileName(String fileName);
    Optional<List<Outfit>> findAllByUserIdAndCategory(Long userId, OutfitCategory category);
}
