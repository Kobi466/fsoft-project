package com.fivetuat.fsoftprojectserver.entities;

import com.fivetuat.fsoftprojectserver.enums.DeckVisibility;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "decks")
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
        this.totalCards=source.totalCards;
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

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

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
