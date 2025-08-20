package com.bolezni.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "_user")
@Entity
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

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

    @Column(name = "is_verified")
    @Builder.Default
    private boolean verified = false;

    @ElementCollection(fetch = FetchType.EAGER, targetClass = Roles.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "roles")
    @Builder.Default
    private Set<Roles> roles = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "author", cascade = CascadeType.ALL)
    @Column(name = "project")
    @Builder.Default
    private List<ProjectEntity> projects = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    @Builder.Default
    private List<EmailVerificationToken> emailVerificationTokens = new ArrayList<>();

    @OneToMany(mappedBy = "reviewedUser", fetch = FetchType.LAZY)
    @Builder.Default
    private List<ReviewEntity> receivedReviewEntities = new ArrayList<>();

    @OneToMany(mappedBy = "reviewer", fetch = FetchType.LAZY)
    @Builder.Default
    private List<ReviewEntity> givenReviewEntities = new ArrayList<>();
}
