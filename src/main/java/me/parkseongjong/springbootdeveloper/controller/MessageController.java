package me.parkseongjong.springbootdeveloper.controller;

import lombok.RequiredArgsConstructor;
import me.parkseongjong.springbootdeveloper.domain.Message;
import me.parkseongjong.springbootdeveloper.domain.User;
import me.parkseongjong.springbootdeveloper.dto.SendMessageRequest;
import me.parkseongjong.springbootdeveloper.service.MessageService;
import org.apache.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpResponse;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/message")
public class MessageController {
    private final MessageService messageService;

    @GetMapping
    public ResponseEntity<List<Message>> listMyMessage(@AuthenticationPrincipal User user) {
        List<Message> messages = messageService.findAllMessageByUserId(user.getId());
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/send")
    public ResponseEntity<Message> sendMyMessage(@AuthenticationPrincipal User user, @RequestBody SendMessageRequest sendMessageRequest) {
        sendMessageRequest.setSender(user.getId());
        Message sendedMessage = messageService.sendMessage(sendMessageRequest);
        return ResponseEntity.ok(sendedMessage);
    }
}
