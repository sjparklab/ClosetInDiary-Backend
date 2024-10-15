package me.parkseongjong.springbootdeveloper.dto;

import lombok.Data;

@Data
public class SignupRequest {
    private String email;
    private String password;
}