package com.fivetuat.fsoftprojectserver.dtos.request;

import lombok.Data;

@Data
public class SubmitAnswerRequest {
    private Long cardId;
    private Integer questionIndex;
}