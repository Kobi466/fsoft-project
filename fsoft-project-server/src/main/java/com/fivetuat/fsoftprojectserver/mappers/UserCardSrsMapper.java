package com.fivetuat.fsoftprojectserver.mappers;

import com.fivetuat.fsoftprojectserver.dtos.response.ReviewCardResponse;
import com.fivetuat.fsoftprojectserver.entities.UserCardSrs;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {CardMapper.class})
public interface UserCardSrsMapper {
	ReviewCardResponse toResponse(UserCardSrs userCardSrs);
}
