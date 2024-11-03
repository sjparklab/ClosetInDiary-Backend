package me.parkseongjong.springbootdeveloper.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class AccessControl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "status_id")
    private Status status;

    @ManyToOne
    @JoinColumn(name = "allowed_user_id")
    private User allowedUser;
}
