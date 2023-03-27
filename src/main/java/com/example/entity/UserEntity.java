package com.example.entity;

import com.example.enums.Role;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor

@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    private Long chatId;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column
    private LocalDateTime registerAt;

    @Enumerated(value = EnumType.STRING)
    @Column
    private Role role;
}
