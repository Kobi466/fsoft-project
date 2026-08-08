package com.fivetuat.fsoftprojectserver.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id", nullable = false)
    Deck deck;

    @Column(name = "word", nullable = false)
    String word;

    @Column(name = "phonetic")
    String phonetic;

    @Column(name = "part_of_speech")
    String partOfSpeech;

    @Column(name = "meaning", nullable = false, columnDefinition = "TEXT")
    String meaning;

    @Column(name = "definition_en", columnDefinition = "TEXT")
    String definitionEn;

    @Column(name = "example_sentence", columnDefinition = "TEXT")
    String exampleSentence;

    @Column(name = "example_meaning", columnDefinition = "TEXT")
    String exampleMeaning;

    @Column(name = "image_url")
    String imageUrl;

    @Column(name = "audio_url")
    String audioUrl;

    @Column(name = "note", columnDefinition = "TEXT")
    String note;

    @Column(name = "position")
    Integer position;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    Instant updatedAt;
}