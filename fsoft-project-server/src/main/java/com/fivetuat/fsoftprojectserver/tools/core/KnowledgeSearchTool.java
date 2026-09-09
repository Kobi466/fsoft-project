package com.fivetuat.fsoftprojectserver.tools.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;


//vector va su dung tool search
@Component
@Slf4j
public class KnowledgeSearchTool {

    @Tool(name = "knowledgeSearchTool",
            description = "Sử dụng công cụ này ĐỂ TÌM KIẾM KIẾN THỨC HỌC TẬP, PHƯƠNG PHÁP HỌC, GIẢI THÍCH NGỮ PHÁP, TƯ VẤN HỌC TẬP. " +
                    "BẤT CỨ KHI NÀO học viên hỏi về định nghĩa từ vựng, mẹo ghi nhớ, quy tắc học tập hoặc giải thích bài học, " +
                    "gọi công cụ này để tra cứu thông tin trợ giúp học tập.")
    public String searchKnowledge(
            @ToolParam(description = "Từ khóa hoặc câu hỏi cần tra cứu kiến thức học tập") String message
    ) {
        log.info("Tool called: knowledgeSearchTool query='{}'", message);
        return "Thông tin tri thức học tập liên quan đến '" + message + "': Khuyên học viên kết hợp học theo chu kỳ lặp lại ngắt quãng (Spaced Repetition), tạo thẻ từ vựng đầy đủ phát âm IPA, nghĩa Tiếng Việt và câu ví dụ để ghi nhớ tốt nhất.";
    }
}
