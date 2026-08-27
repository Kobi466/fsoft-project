package com.fivetuat.fsoftprojectserver.services.implement;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fivetuat.fsoftprojectserver.dtos.request.TagRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.TagResponse;
import com.fivetuat.fsoftprojectserver.entities.Tag;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.mappers.TagMapper;
import com.fivetuat.fsoftprojectserver.repositories.TagRepository;
import com.fivetuat.fsoftprojectserver.services.TagService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;


@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TagServiceImpl implements TagService {

    TagRepository tagRepository;
    TagMapper tagMapper;

    @Override
    @Transactional
    public TagResponse createTag(TagRequest request) {
        String name = request.getName().trim();
        String slug = toSlug(name);

        log.info("Creating tag with name: {} and slug: {}", name, slug);

        if (tagRepository.existsByName(name) || tagRepository.existsBySlug(slug)) {
            throw new AppException(ErrorCode.TAG_NAME_EXISTS);
        }

        Tag tag = Tag.builder()
                .name(name)
                .slug(slug)
                .build();

        Tag savedTag = tagRepository.save(tag);
        return tagMapper.toTagResponse(savedTag);
    }

    @Override
    public List<TagResponse> getAllTags() {
        log.info("Retrieving all tags");
        return tagRepository.findAll().stream()
                .map(this.tagMapper::toTagResponse)
                .toList();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public TagResponse updateTag(Long id, TagRequest request) {
        log.info("Updating tag ID: {}", id);
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TAG_NOT_FOUND));

        String name = request.getName().trim();
        String slug = toSlug(name);

        // Check duplicate name or slug only if it's changing
        if (!tag.getName().equalsIgnoreCase(name) && 
            (tagRepository.existsByName(name) || tagRepository.existsBySlug(slug))) {
            throw new AppException(ErrorCode.TAG_NAME_EXISTS);
        }

        tag.setName(name);
        tag.setSlug(slug);
        Tag updatedTag = tagRepository.save(tag);

        return tagMapper.toTagResponse(updatedTag);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteTag(Long id) {
        log.info("Deleting tag ID: {}", id);
        if (!tagRepository.existsById(id)) {
            throw new AppException(ErrorCode.TAG_NOT_FOUND);
        }
        tagRepository.deleteById(id);
    }

    private String toSlug(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        String nonWhitespace = input.trim().replaceAll("\\s+", "-");
        String normalized = Normalizer.normalize(nonWhitespace, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String slug = pattern.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH)
                .replaceAll("[^a-z0-9\\-]", "")
                .replaceAll("-+", "-");
    }
}
