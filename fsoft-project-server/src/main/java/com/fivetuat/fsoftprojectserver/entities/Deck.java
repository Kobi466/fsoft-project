package com.fivetuat.fsoftprojectserver.entities;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.fivetuat.fsoftprojectserver.enums.DeckVisibility;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
@Table(name = "decks", indexes = {
    @Index(name = "idx_decks_profile_deleted", columnList = "profile_id, is_deleted"),
    @Index(name = "idx_decks_visibility_deleted", columnList = "visibility, is_deleted"),
    @Index(name = "idx_decks_official_deleted", columnList = "is_official, is_deleted")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Deck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    public Deck(Deck source) {
        this.title = source.title;
        this.description = source.description;
        this.coverImageUrl = source.coverImageUrl;
        this.sourceLanguage = source.sourceLanguage;
        this.targetLanguage = source.targetLanguage;
        this.totalCards = source.totalCards;
        this.isOfficial = source.isOfficial;
    }
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    Profile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_deck_id")
    Deck parentDeck;

    @OneToMany(mappedBy = "parentDeck", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<Deck> subDecks;

    @Column(nullable = false)
    String title;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(name = "cover_image_url")
    String coverImageUrl;

    @Column(name = "source_language", nullable = false)
    @Builder.Default
    String sourceLanguage = "en";

    @Column(name = "target_language", nullable = false)
    @Builder.Default
    String targetLanguage = "vi";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    DeckVisibility visibility;

    @Column(name = "is_official", nullable = false)
    boolean isOfficial;

    @Column(name = "total_cards", nullable = false)
    int totalCards;

    @Column(name = "fork_count", nullable = false)
    int forkCount;

    @Column(name = "is_deleted", nullable = false)
    boolean isDeleted;

    @Column(name = "is_share_link_enabled", nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    boolean isShareLinkEnabled = false;

    @Column(name = "share_token", unique = true)
    String shareToken;

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @Column(name = "pending_public", nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    boolean pendingPublic = false;
    @OneToMany(mappedBy = "deck", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    List<Card> cards;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "deck_tags",
            joinColumns = @JoinColumn(name = "deck_id"),
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
