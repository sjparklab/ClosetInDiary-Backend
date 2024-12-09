package me.parkseongjong.springbootdeveloper.dto;

import lombok.*;
import me.parkseongjong.springbootdeveloper.domain.User;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDiaryRequest {
    private Long id;
    private String title;
    private String content;
    private String date;
    private User user;
    private String mainImagePath;       // 추가
    private List<Long> outfitIds;
}
