package me.parkseongjong.springbootdeveloper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryResponse {
    private Long id;
    private LocalDate date;
    private String emotion;
    private String content;
    private String mainImagePath;
    private List<String> subImagePaths;
    // user 정보나 다른 연관 엔티티 정보가 필요하다면 추가 가능
}
