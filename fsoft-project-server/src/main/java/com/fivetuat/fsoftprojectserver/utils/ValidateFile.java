package com.fivetuat.fsoftprojectserver.utils;

import org.springframework.web.multipart.MultipartFile;

public class ValidateFile {
    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024; // 20MB

    private static final java.util.Set<String> BLOCKED_EXTENSIONS = java.util.Set.of(
            "exe", "bat", "cmd", "sh", "bin", "msi", "jar", "vbs", "com", "scr", "pif"
    );

    public static void validate(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "Dung lượng file không được vượt quá 20MB"
            );
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            if (BLOCKED_EXTENSIONS.contains(extension)) {
                throw new IllegalArgumentException(
                        "Định dạng file không được phép tải lên vì lý do bảo mật: ." + extension
                );
            }
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new IllegalArgumentException("Không xác định được loại file");
        }

        boolean isAllowedType = contentType.startsWith("image/")
                || contentType.startsWith("audio/")
                || contentType.startsWith("video/")
                || contentType.startsWith("text/")
                || contentType.equals("application/pdf")
                || contentType.equals("application/msword")
                || contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                || contentType.equals("application/vnd.ms-excel")
                || contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                || contentType.equals("application/vnd.ms-powerpoint")
                || contentType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")
                || contentType.equals("application/zip")
                || contentType.equals("application/x-zip-compressed")
                || contentType.equals("application/x-rar-compressed")
                || contentType.equals("application/vnd.rar")
                || contentType.equals("application/x-7z-compressed")
                || contentType.equals("application/octet-stream");

        if (!isAllowedType) {
            throw new IllegalArgumentException(
                    "Loại file không được hỗ trợ: " + contentType
            );
        }
    }
}
