package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.httpsClients.DictionaryClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DictionaryService {
    
    DictionaryClient dictionaryClient;
    
    @Cacheable(value = "dictionary", key = "#word.toLowerCase().trim()", unless = "#result == null")
    public Object lookupWord(String word) {
        log.info("Tra từ điển cho từ: {}", word);
        try {
            return dictionaryClient.lookupWord(word.toLowerCase().trim());
        } catch (Exception e) {
            log.error("Lỗi khi tra từ điển: {}", e.getMessage());
            throw new AppException(ErrorCode.DICTIONARY_LOOKUP_ERROR);
        }
    }
}
