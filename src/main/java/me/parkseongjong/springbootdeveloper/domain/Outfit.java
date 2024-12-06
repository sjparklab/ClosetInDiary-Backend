package me.parkseongjong.springbootdeveloper.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Table(name = "outfit")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity
public class Outfit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private OutfitCategory category;

    @Column(nullable = false, length = 500)
    private String reason; // 구매 계기

    @Column(nullable = false)
    private LocalDate purchaseDate; // 구매일

    @Column(nullable = false, length = 100)
    private String brand; // 브랜드

    @Column(nullable = false, length = 50)
    private String size; // 사이즈

    @JsonIgnore
    @Column(name = "file_key")
    private String fileKey;

    @JsonIgnore
    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "file_name")
    private String fileName;

    // Diary 엔티티와 양방향 매핑
    @ManyToMany(mappedBy = "outfits")
    private List<Diary> diaries;

    @Builder
    public Outfit(User user, String fileKey, OutfitCategory category, String reason, LocalDate purchaseDate,String brand, String size, String imageUrl, String fileName) {
        this.user = user;
        this.fileKey = fileKey;
        this.category = category;
        this.reason = reason;
        this.purchaseDate = purchaseDate;
        this.brand = brand;
        this.size = size;
        this.imageUrl = imageUrl;
        this.fileName = fileName;
    }
}