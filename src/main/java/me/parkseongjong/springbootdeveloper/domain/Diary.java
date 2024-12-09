package me.parkseongjong.springbootdeveloper.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Table(name = "diary")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity
public class Diary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "title")
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    // 메인 이미지 경로
    @Column(name = "main_image_path")
    private String mainImagePath;

    // 다시 outfits 리스트 추가
    @ManyToMany
    @JoinTable(
            name = "diary_outfit",
            joinColumns = @JoinColumn(name = "diary_id"),
            inverseJoinColumns = @JoinColumn(name = "outfit_id")
    )
    private List<Outfit> outfits;

    @Builder
    public Diary(User user, LocalDate date, String title, String content, String mainImagePath, List<Outfit> outfits) {
        this.user = user;
        this.date = date;
        this.title = title;
        this.content = content;
        this.mainImagePath = mainImagePath;
        this.outfits = outfits;
    }
}
