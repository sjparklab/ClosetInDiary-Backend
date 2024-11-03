package me.parkseongjong.springbootdeveloper.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class Status {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private boolean isPublic;

    @OneToMany(mappedBy = "status")
    private List<AccessControl> accessControls;
}