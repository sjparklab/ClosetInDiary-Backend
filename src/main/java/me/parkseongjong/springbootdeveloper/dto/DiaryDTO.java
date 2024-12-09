// DiaryDTO.java
package me.parkseongjong.springbootdeveloper.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DiaryDTO {
    private Long id;
    private LocalDate date;
    private String title;
    private String content;
    private String mainImagePath;

    public DiaryDTO(Long id, LocalDate date, String title, String content, String mainImagePath) {
        this.id = id;
        this.date = date;
        this.title = title;
        this.content = content;
        this.mainImagePath = mainImagePath;
    }
}