package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.enums.FolderName;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    String uploadFile(MultipartFile file, FolderName folder);
}
