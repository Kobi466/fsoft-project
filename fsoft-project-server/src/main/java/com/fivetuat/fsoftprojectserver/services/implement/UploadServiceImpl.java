package com.fivetuat.fsoftprojectserver.services.implement;

import com.fivetuat.fsoftprojectserver.enums.FolderName;
import com.fivetuat.fsoftprojectserver.services.UploadService;
import com.fivetuat.fsoftprojectserver.utils.ValidateFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;
@Slf4j
@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    @Override
    public String uploadFile(MultipartFile file, FolderName folderName ) {
        ValidateFile.validate(file);
        try {
            String fileName = UUID.randomUUID()
                    + "_" + file.getOriginalFilename();

            String key = folderName.getPath() + "/" + fileName;


            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromBytes(file.getBytes())
            );

            // URL public của file
            return "https://" + bucketName
                    + ".s3." + region
                    + ".amazonaws.com/" + key;

        } catch (IOException e) {
            throw new RuntimeException("Không thể đọc file", e);
        } catch (Exception e) {
            log.error("Upload file lên S3 thất bại", e);
            throw new RuntimeException("Upload file lên S3 thất bại", e);
        }
    }
}