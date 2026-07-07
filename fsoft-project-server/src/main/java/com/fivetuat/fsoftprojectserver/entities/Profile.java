package com.fivetuat.fsoftprojectserver.entities;

import com.fivetuat.fsoftprojectserver.entities.authentication.Auth;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "profiles")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Profile {
    @Id
    UUID id;
    @OneToOne(
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL
    )
    @MapsId
    @JoinColumn(name = "id")
    Auth auth;
    String fullName;
    String avatar;

    @Lob
    String about;
    String personalWebsite;
    String github;
    String linkedin;
    String facebook;
    String youtube;

//    @OneToMany(mappedBy = "profile", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
//    Set<Post> posts;
//
//    @OneToMany(mappedBy = "creator", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
//    Set<Course> courses;
//
//    @OneToMany(mappedBy = "profile", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
//    Set<Enrollment> enrollments;
//
//    @OneToMany(mappedBy = "profile", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
//    Set<Order> orders;

    LocalDateTime createTime;
    LocalDateTime updateTime;

    @PrePersist
    public void prePersist() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }
}
