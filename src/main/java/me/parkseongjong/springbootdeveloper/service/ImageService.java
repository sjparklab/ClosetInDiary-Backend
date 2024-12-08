package me.parkseongjong.springbootdeveloper.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import lombok.RequiredArgsConstructor;
import me.parkseongjong.springbootdeveloper.repository.OutfitRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final AmazonS3 s3Client;

    private final OutfitRepository outfitRepository;

    private static final String BUCKET_NAME = "closetindiary-image-bucket";
    public String uploadFileToS3(MultipartFile file, String userId) {
        String fileKey = "user_" + userId + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            s3Client.putObject(new PutObjectRequest(BUCKET_NAME, fileKey, file.getInputStream(), metadata));
            return fileKey;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        }
        return convFile;
    }

    @Cacheable(value = "profilePictures")
    public byte[] getImageFromS3(String fileKey) throws IOException {
        try (S3Object s3Object = s3Client.getObject(BUCKET_NAME, fileKey)) {
            return s3Object.getObjectContent().readAllBytes();
        }
    }
}
