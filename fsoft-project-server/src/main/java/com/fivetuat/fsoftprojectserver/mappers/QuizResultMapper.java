package com.fivetuat.fsoftprojectserver.mappers;

import com.fivetuat.fsoftprojectserver.dtos.response.QuizResultResponse;
import com.fivetuat.fsoftprojectserver.entities.QuizResult;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface QuizResultMapper {

    QuizResultResponse toQuizResultResponse(QuizResult quizResult);

    List<QuizResultResponse> toQuizResultResponseList(List<QuizResult> quizResults);
}
