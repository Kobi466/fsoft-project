package com.fivetuat.fsoftprojectserver.entities;

import com.fivetuat.fsoftprojectserver.enums.PostStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "community_posts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommunityPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    Profile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id")
    Deck deck;

    @Column(nullable = false)
    String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    PostStatus status = PostStatus.PENDING;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    Profile approvedBy;

    @Column(name = "approved_at")
    LocalDateTime approvedAt;

    @Column(name = "like_count", nullable = false)
    @Builder.Default
    int likeCount = 0;

    @Column(name = "comment_count", nullable = false)
    @Builder.Default
    int commentCount = 0;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    boolean isDeleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "community_post_tags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    Set<Tag> tags;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
