package com.fivetuat.fsoftprojectserver.dtos.request;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request để phân loại từ vựng tự động (Magic Sort)")
public record MagicSortRequest(
    @Schema(description = "Danh sách các từ vựng cần phân loại", example = "[\"Apple\", \"Laptop\"]")
    List<String> words,
    
    @Schema(description = "Danh sách các Deck hiện có của User dưới dạng Map (ID -> Tên)", example = "{\"1\": \"Trái cây\", \"2\": \"Công nghệ\"}")
    Map<Long, String> decks
) {}
