package com.fivetuat.fsoftprojectserver.httpsClients;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface DictionaryClient {
    
    @GetExchange(value = "/api/v2/entries/en/{word}")
    Object lookupWord(@PathVariable("word") String word);
}
