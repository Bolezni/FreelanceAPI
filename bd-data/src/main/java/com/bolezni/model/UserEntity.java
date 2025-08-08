package com.bolezni.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "_user")
@Entity
public class UserEntity extends BaseEntity {

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column
    private String provider;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "verification_token ")
    private String verificationToken;

    @Column(name = "expiration_token")
    private LocalDateTime expirationToken;

    @Column(name = "is_verified")
    @Builder.Default
    private boolean isVerified = false;

    @ElementCollection(fetch = FetchType.EAGER, targetClass = Roles.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "roles")
    @Builder.Default
    private Set<Roles> roles = new HashSet<>();
}
