package com.fivetuat.fsoftprojectserver.utils;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;

public class PageUtils {

    private PageUtils() {
    }

    public static Pageable createPageable(int page, int size) {
        int pageNo = Math.max(1, page);
        return PageRequest.of(pageNo - 1, size);
    }

    public static <T, R> PageResponse<R> toPageResponse(Page<T> springPage, Function<T, R> mapper) {
        List<R> content = springPage.getContent().stream()
                .map(mapper)
                .toList();

        return PageResponse.<R>builder()
                .pageNo(springPage.getNumber() + 1)
                .pageSize(springPage.getSize())
                .totalPages(springPage.getTotalPages())
                .totalElements(springPage.getTotalElements())
                .content(content)
                .last(springPage.isLast())
                .build();
    }
}
