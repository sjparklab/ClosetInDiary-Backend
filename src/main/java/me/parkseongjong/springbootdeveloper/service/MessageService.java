package me.parkseongjong.springbootdeveloper.service;

import lombok.RequiredArgsConstructor;
import me.parkseongjong.springbootdeveloper.domain.Message;
import me.parkseongjong.springbootdeveloper.domain.User;
import me.parkseongjong.springbootdeveloper.dto.SendMessageRequest;
import me.parkseongjong.springbootdeveloper.repository.MessageRepository;
import me.parkseongjong.springbootdeveloper.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    public List<Message> findAllMessageByUserId(Long userId) {
        return messageRepository.findAllByReceiverId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Not Found ID"));
    }

    public Message sendMessage(SendMessageRequest sendMessageRequest) {
        User sender = userRepository.findById(sendMessageRequest.getSender()).orElseThrow(() -> new IllegalArgumentException("Not Found ID"));
        User receiver = userRepository.findById(sendMessageRequest.getReceiver()).orElseThrow(() -> new IllegalArgumentException("Not Found ID"));
        Message savedMessage = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .isRead(false)
                .content(sendMessageRequest.getContent())
                .timestamp(LocalDateTime.now())
                .build();

        return messageRepository.save(savedMessage);
    }
}
