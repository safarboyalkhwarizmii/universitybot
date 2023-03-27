package com.example.entity;

import com.example.enums.AdminStep;
import com.example.enums.UserStep;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "admin_history")
public class AdminHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id")
    private Long adminId;

    @ManyToOne
    @JoinColumn(name = "admin_id", insertable = false, updatable = false)
    private UserEntity admin;

    @Column
    @Enumerated(value = EnumType.STRING)
    private AdminStep step;

    @Column
    private String value;
}
