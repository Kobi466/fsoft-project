package com.fivetuat.fsoftprojectserver.utils;

import org.springframework.web.multipart.MultipartFile;

public class ValidateFile {
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public static void validate(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "Dung lượng file không được vượt quá 10MB"
            );
        }

        String contentType = file.getContentType();

        if (contentType == null) {
            throw new IllegalArgumentException("Không xác định được loại file");
        }

        if (!contentType.startsWith("image/")
                && !contentType.startsWith("audio/")
                && !contentType.startsWith("video/")
                && !contentType.equals("application/pdf")) {

            throw new IllegalArgumentException(
                    "Loại file không được hỗ trợ: " + contentType
            );
        }
    }
}
