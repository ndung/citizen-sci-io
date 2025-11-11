package io.sci.citizen.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "app_user",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
        })
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {
    @EqualsAndHashCode.Include
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String username;

    @Column(nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, length = 160)
    private String email;

    @JsonIgnore
    @Column(nullable = false, length = 255)
    private String passwordHash;         // store encoded password

    @JsonIgnore
    private boolean enabled = true;

    @JsonIgnore
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles = new LinkedHashSet<>();

    @JsonIgnore
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Override
    public String toString() {
        return fullName;
    }
}