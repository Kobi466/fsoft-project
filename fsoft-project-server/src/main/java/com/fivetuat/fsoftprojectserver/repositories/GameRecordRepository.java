package com.fivetuat.fsoftprojectserver.repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fivetuat.fsoftprojectserver.entities.GameRecord;

@Repository
public interface GameRecordRepository extends JpaRepository<GameRecord, String> {

    @Query("SELECT g FROM GameRecord g " +
            "LEFT JOIN FETCH g.user u " +
            "LEFT JOIN FETCH u.profile " +
            "WHERE g.deck.id = :deckId AND g.gameType = :gameType " +
            "ORDER BY g.score DESC, g.completionTime ASC")
    List<GameRecord> findTopLeaderboard(@Param("deckId") Long deckId,
                                        @Param("gameType") String gameType,
                                        Pageable pageable);
}