package com.fivetuat.fsoftprojectserver.services;

import java.util.List;

import com.fivetuat.fsoftprojectserver.dtos.request.AudioReflexSubmitRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.AudioReflexGameResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.AudioReflexLeaderboardResponse;

public interface AudioReflexGameService {
    AudioReflexGameResponse generateGame(Long deckId, int questionCount);
    AudioReflexLeaderboardResponse submitResult(AudioReflexSubmitRequest request, String currentUserId);
    List<AudioReflexLeaderboardResponse> getLeaderboard(Long deckId, int limit);
}
