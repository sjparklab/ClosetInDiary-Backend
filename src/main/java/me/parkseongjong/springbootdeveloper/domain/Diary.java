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

    @Column(name = "emotion")
    private String emotion;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    // Diary와 Outfit의 다대다 관계 설정
    @ManyToMany
    @JoinTable(
            name = "diary_outfit",
            joinColumns = @JoinColumn(name = "diary_id"),
            inverseJoinColumns = @JoinColumn(name = "outfit_id")
    )
    private List<Outfit> outfits; // 선택된 착장 목록을 저장할 리스트

    @Builder
    public Diary(User user, LocalDate date, String emotion, List<Outfit> outfits, String content) {
        this.user = user;
        this.date = date;
        this.emotion = emotion;
        this.outfits = outfits;
        this.content = content;
    }
}
