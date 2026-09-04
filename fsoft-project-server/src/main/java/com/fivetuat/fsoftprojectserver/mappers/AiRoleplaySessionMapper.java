package com.fivetuat.fsoftprojectserver.mappers;

import com.fivetuat.fsoftprojectserver.dtos.request.CreateAiRoleplaySessionRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.AiRoleplaySessionResponse;
import com.fivetuat.fsoftprojectserver.entities.AiRoleplaySession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface AiRoleplaySessionMapper {

    @Mapping(target = "targetWords", source = "targetWords", qualifiedByName = "stringToList")
    AiRoleplaySessionResponse toResponse(AiRoleplaySession session);

    List<AiRoleplaySessionResponse> toResponseList(List<AiRoleplaySession> sessions);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "targetWords", source = "targetWords", qualifiedByName = "listToString")
    AiRoleplaySession toEntity(CreateAiRoleplaySessionRequest request);

    @Named("stringToList")
    default List<String> stringToList(String targetWords) {
        if (targetWords == null || targetWords.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.asList(targetWords.split(",\\s*"));
    }

    @Named("listToString")
    default String listToString(List<String> targetWords) {
        if (targetWords == null || targetWords.isEmpty()) {
            return "";
        }
        return String.join(", ", targetWords);
    }
}
