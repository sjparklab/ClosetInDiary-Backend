package me.parkseongjong.springbootdeveloper.dto;

import lombok.Getter;
import lombok.Setter;
import me.parkseongjong.springbootdeveloper.domain.User;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class UpdateDiaryRequest {
    private Long id;
    private User user;
    private LocalDate date;
    private String emotion;
    private String content;
    private List<Long> outfitIds;
}
