package me.parkseongjong.springbootdeveloper.dto;

import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String name;
    private String onelineInfo;
    private String profilePicture;

    public UserResponse(Long id, String username, String email, String name, String onelineInfo, String profilePicture) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.name = name;
        this.onelineInfo = onelineInfo;
        this.profilePicture = profilePicture;
    }

    // Getter와 Setter 생략
}