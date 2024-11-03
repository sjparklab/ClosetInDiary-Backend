package me.parkseongjong.springbootdeveloper.repository;

import me.parkseongjong.springbootdeveloper.domain.AccessControl;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessControlRepository extends JpaRepository<AccessControl, Long> {
}
