package com.fivetuat.fsoftprojectserver.services.implement;

import com.fivetuat.fsoftprojectserver.services.UploadService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UploadServiceImpl implements UploadService {
    @Override
    public String uploadFile(MultipartFile file) {
        // Tạm thời chưa triển khai, trả về null
        return null;
    }
}
