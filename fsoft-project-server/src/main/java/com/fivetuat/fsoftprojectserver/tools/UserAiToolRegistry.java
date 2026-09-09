package com.fivetuat.fsoftprojectserver.tools;

import com.fivetuat.fsoftprojectserver.tools.core.KnowledgeSearchTool;
import com.fivetuat.fsoftprojectserver.tools.deck.UserDeckTools;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserAiToolRegistry {

    private final UserDeckTools userDeckTools;
    private final KnowledgeSearchTool knowledgeSearchTool;

   
    public Object[] getAllTools() {
        return new Object[] {
            userDeckTools
        };
    }

    public List<Object> getAllToolsAsList() {
        return Arrays.asList(getAllTools());
    }
}
