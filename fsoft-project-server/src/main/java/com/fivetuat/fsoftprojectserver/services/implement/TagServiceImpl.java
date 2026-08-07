package com.fivetuat.fsoftprojectserver.services.implement;

import com.fivetuat.fsoftprojectserver.dtos.request.TagCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.TagResponse;
import com.fivetuat.fsoftprojectserver.entities.Tag;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.repositories.TagRepository;
import com.fivetuat.fsoftprojectserver.services.TagService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TagServiceImpl implements TagService {
    TagRepository tagRepository;

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    @Override
    @Transactional
    public TagResponse createTag(TagCreateRequest request) {
        String tagName = request.getName().trim();
        if (tagRepository.existsByName(tagName)) {
            throw new AppException(ErrorCode.TAG_ALREADY_EXISTS);
        }

        String slug = toSlug(tagName);
        Tag tag = Tag.builder()
                .name(tagName)
                .slug(slug)
                .build();

        tag = tagRepository.save(tag);
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .build();
    }

    @Override
    public List<TagResponse> getAllTags() {
        return tagRepository.findAll().stream()
                .map(tag -> TagResponse.builder()
                        .id(tag.getId())
                        .name(tag.getName())
                        .build())
                .collect(Collectors.toList());
    }

    private String toSlug(String input) {
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        slug = slug.toLowerCase(Locale.ENGLISH).replaceAll("-+", "-").replaceAll("^-|-$", "");
        return slug.isEmpty() ? "tag-" + System.currentTimeMillis() : slug;
    }
}
