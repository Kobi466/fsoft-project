package com.fivetuat.fsoftprojectserver.repositories;

import com.fivetuat.fsoftprojectserver.entities.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike> findByCommunityPostIdAndProfileId(Long communityPostId, UUID profileId);

    boolean existsByCommunityPostIdAndProfileId(Long communityPostId, UUID profileId);

    void deleteByCommunityPostIdAndProfileId(Long communityPostId, UUID profileId);

    long countByCommunityPostId(Long communityPostId);

    @Query("SELECT pl.communityPost.id FROM PostLike pl WHERE pl.profile.id = :profileId AND pl.communityPost.id IN :postIds")
    Set<Long> findLikedPostIdsByProfileIdAndPostIdsIn(
            @Param("profileId") UUID profileId,
            @Param("postIds") List<Long> postIds
    );
}
