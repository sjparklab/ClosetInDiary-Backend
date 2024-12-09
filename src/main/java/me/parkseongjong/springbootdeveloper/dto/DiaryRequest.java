package me.parkseongjong.springbootdeveloper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.parkseongjong.springbootdeveloper.domain.User;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryRequest {
    private String title;
    private String content;
    private String date;
    private User user;
    private String mainImagePath;       // 추가
    private List<Long> outfitIds;
}