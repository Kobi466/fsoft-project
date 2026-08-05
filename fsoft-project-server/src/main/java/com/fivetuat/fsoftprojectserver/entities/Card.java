package com.fivetuat.fsoftprojectserver.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "cards")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id", nullable = false)
    Deck deck;

    @Column(nullable = false)
    String word;

    String phonetic;

    @Column(name = "part_of_speech")
    String partOfSpeech;

    @Lob
    @Column(columnDefinition = "TEXT")
    String meaning;

    @Lob
    @Column(name = "definition_en", columnDefinition = "TEXT")
    String definitionEn;

    @Lob
    @Column(name = "example_sentence", columnDefinition = "TEXT")
    String exampleSentence;

    @Lob
    @Column(name = "example_meaning", columnDefinition = "TEXT")
    String exampleMeaning;

    @Column(name = "image_url")
    String imageUrl;

    @Column(name = "audio_url")
    String audioUrl;

    @Lob
    @Column(columnDefinition = "TEXT")
    String note;

    @Column(nullable = false)
    int position;

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    List<UserCardSrs> srsRecords;

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    List<QuizDetail> quizDetails;

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
