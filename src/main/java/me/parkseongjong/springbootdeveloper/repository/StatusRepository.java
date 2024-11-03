package me.parkseongjong.springbootdeveloper.repository;

import me.parkseongjong.springbootdeveloper.domain.Status;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusRepository extends JpaRepository<Status, Long> {
}
