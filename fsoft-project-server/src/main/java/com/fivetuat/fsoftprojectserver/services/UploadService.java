package com.fivetuat.fsoftprojectserver.services;

import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    String uploadFile(MultipartFile file);
}
