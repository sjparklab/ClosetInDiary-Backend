package me.parkseongjong.springbootdeveloper.domain;

import jakarta.persistence.*;
import lombok.*;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "date", nullable = false)
    private String date;

    @Column(name = "emotion")
    private String emotion;

    @Column(name = "outfit_ids")
    private String outfitIds;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Builder
    public Diary(User user, String date, String emotion, String outfitIds, String content) {
        this.user = user;
        this.date = date;
        this.emotion = emotion;
        this.outfitIds = outfitIds;
        this.content = content;
    }
}
