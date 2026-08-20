package com.fivetuat.fsoftprojectserver.mappers;

import com.fivetuat.fsoftprojectserver.dtos.response.SrsCardResponse;
import com.fivetuat.fsoftprojectserver.entities.UserCardSrs;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {CardMapper.class})
public interface UserCardSrsMapper {
	SrsCardResponse toResponse(UserCardSrs userCardSrs);
}
