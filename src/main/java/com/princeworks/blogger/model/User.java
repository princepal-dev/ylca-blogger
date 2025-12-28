package com.princeworks.blogger.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Data
@Entity
@NoArgsConstructor
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username")
})
public class User {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @NotBlank
    @Size(max = 30)
    @Column(name = "username", nullable = false, length = 30, unique = true)
    private String userName;

    @NotBlank
    @Size(max = 60)
    @Column (nullable = false, length = 60)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column (nullable = false)
    private AppRole role;

    public User(String userName, String password, AppRole role) {
        this.role = role;
        this.userName = userName;
        this.password = password;
    }

    @Column (length = 30)
    private String fullName;

    @Column (length = 10)
    private String phoneNumber;

    @CreationTimestamp
    private Timestamp createdAt;
}
