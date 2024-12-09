package me.parkseongjong.springbootdeveloper.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import me.parkseongjong.springbootdeveloper.repository.OutfitRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;

import java.io.FileInputStream;
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
            // Convert MultipartFile to File
            File convFile = convertMultiPartToFile(file);

            // Resize the image
            File resizedFile = new File("resized_" + convFile.getName());
            Thumbnails.of(convFile)
                    .size(1024, 768)
                    .outputQuality(0.8)
                    .toFile(resizedFile);

            // Upload resized file to S3
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(resizedFile.length());
            s3Client.putObject(new PutObjectRequest(BUCKET_NAME, fileKey, new FileInputStream(resizedFile), metadata));

            // Clean up temporary files
            convFile.delete();
            resizedFile.delete();

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

    public void resizeAllImages() {
        ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(BUCKET_NAME);
        ListObjectsV2Result result;
        do {
            result = s3Client.listObjectsV2(req);

            for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                String key = objectSummary.getKey();
                try {
                    // Download the image from S3
                    File originalFile = new File(key);
                    try (FileOutputStream fos = new FileOutputStream(originalFile)) {
                        s3Client.getObject(BUCKET_NAME, key).getObjectContent().transferTo(fos);
                    }

                    // Resize the image
                    File resizedFile = new File("resized_" + originalFile.getName());
                    Thumbnails.of(originalFile)
                            .size(1024, 768)
                            .outputQuality(0.8)
                            .toFile(resizedFile);

                    // Rename the original file and upload it back to S3
                    String originalKey = key.replace(".", "_Original.");
                    s3Client.putObject(new PutObjectRequest(BUCKET_NAME, originalKey, new FileInputStream(originalFile), new ObjectMetadata()));

                    // Upload the resized file to S3, overwriting the original
                    ObjectMetadata metadata = new ObjectMetadata();
                    metadata.setContentLength(resizedFile.length());
                    s3Client.putObject(new PutObjectRequest(BUCKET_NAME, key, new FileInputStream(resizedFile), metadata));

                    // Clean up temporary files
                    originalFile.delete();
                    resizedFile.delete();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to process file: " + key, e);
                }
            }
            req.setContinuationToken(result.getNextContinuationToken());
        } while (result.isTruncated());
    }
}
