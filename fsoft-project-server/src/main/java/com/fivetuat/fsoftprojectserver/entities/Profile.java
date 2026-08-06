package com.fivetuat.fsoftprojectserver.entities;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import com.fivetuat.fsoftprojectserver.entities.authentication.Auth;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

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

    @OneToMany(mappedBy = "profile", fetch = FetchType.LAZY, orphanRemoval = true)
    Set<Deck> decks;

    @OneToOne(mappedBy = "profile", fetch = FetchType.LAZY, orphanRemoval = true)
    UserStreak streak;

    @OneToMany(mappedBy = "profile", fetch = FetchType.LAZY, orphanRemoval = true)
    Set<UserActivity> activities;

    @OneToMany(mappedBy = "profile", fetch = FetchType.LAZY, orphanRemoval = true)
    Set<UserCardSrs> srsRecords;

    @OneToMany(mappedBy = "profile", fetch = FetchType.LAZY, orphanRemoval = true)
    Set<QuizResult> quizResults;

    LocalDateTime createTime;
    LocalDateTime updateTime;

    @PrePersist
    public void prePersist() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updateTime = LocalDateTime.now();
    }
}

