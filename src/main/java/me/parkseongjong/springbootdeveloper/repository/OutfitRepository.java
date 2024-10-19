package me.parkseongjong.springbootdeveloper.repository;

import me.parkseongjong.springbootdeveloper.domain.Outfit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutfitRepository extends JpaRepository<Outfit, Long> {
}
