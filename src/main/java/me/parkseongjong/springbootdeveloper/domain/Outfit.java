package me.parkseongjong.springbootdeveloper.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "folder")
    private String folder;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @JsonIgnore
    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "file_name")
    private String fileName;

    @Builder
    public Outfit(User user, String category, String folder, String description, String imageUrl, String fileName) {
        this.user = user;
        this.category = category;
        this.folder = folder;
        this.description = description;
        this.imageUrl = imageUrl;
        this.fileName = fileName;
    }
}