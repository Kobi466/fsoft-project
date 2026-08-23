package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.DictionaryService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dictionary")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class DictionaryController {
    
    DictionaryService dictionaryService;
    
    @GetMapping("/lookup")
    public ApiResponse<Object> lookupWord(@RequestParam String word) {
        Object result = dictionaryService.lookupWord(word);
        return ApiResponse.ok(result, SuccessCode.DICTIONARY_LOOKUP_SUCCESS);
    }
}
