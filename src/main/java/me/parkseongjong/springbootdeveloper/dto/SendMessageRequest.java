package me.parkseongjong.springbootdeveloper.dto;

import lombok.Data;

@Data
public class SendMessageRequest {
    private String content;
    private Long sender;
    private Long receiver;
}
