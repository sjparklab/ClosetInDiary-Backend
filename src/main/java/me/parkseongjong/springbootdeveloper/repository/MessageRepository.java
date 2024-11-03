package me.parkseongjong.springbootdeveloper.repository;

import me.parkseongjong.springbootdeveloper.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
