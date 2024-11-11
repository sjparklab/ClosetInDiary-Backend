package me.parkseongjong.springbootdeveloper.controller;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import me.parkseongjong.springbootdeveloper.domain.Outfit;
import me.parkseongjong.springbootdeveloper.domain.OutfitCategory;
import me.parkseongjong.springbootdeveloper.domain.User;
import me.parkseongjong.springbootdeveloper.repository.OutfitRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/closet")
@RequiredArgsConstructor
public class OutfitController {
    private final AmazonS3 s3Client;

    private final OutfitRepository outfitRepository;

    private static final String BUCKET_NAME = "closetindiary-image-bucket";

    @PostMapping("/upload")
    public ResponseEntity<String> uploadOutfitImage(@RequestParam("file") MultipartFile file,
                                                    @RequestParam("category") String scategory,
                                                    @RequestParam("folder") String folder,
                                                    @RequestParam("description") String description,
                                                    @AuthenticationPrincipal User user) {
        if (user == null) {
            return new ResponseEntity<>("User not authenticated", HttpStatus.UNAUTHORIZED);
        }

        OutfitCategory category = OutfitCategory.valueOf(scategory.toUpperCase()); //

        String fileKey = System.currentTimeMillis() + "_" + user.getId() + "_" + file.getOriginalFilename();
        String fileName = file.getOriginalFilename();
        File tempFile = convertMultiPartToFile(file);
        s3Client.putObject(new PutObjectRequest(BUCKET_NAME, fileKey, tempFile));
        String imageUrl = s3Client.getUrl(BUCKET_NAME, fileKey).toString();

        // Save outfit metadata to the database
        Outfit outfit = Outfit.builder()
                .user(user)
                .fileKey(fileKey)
                .category(category)
                .folder(folder)
                .description(description)
                .imageUrl(imageUrl)
                .fileName(fileName)
                .build();
        outfitRepository.save(outfit);

        // Delete the temporary file after uploading
        tempFile.delete();

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private File convertMultiPartToFile(MultipartFile file) {
        File convFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return convFile;
    }

    @Cacheable(value = "outfitImages", key = "#id")
    @GetMapping("/image/{id}")
    public ResponseEntity<Resource> getImageFromS3(@PathVariable Long id, @AuthenticationPrincipal User user) {
        System.out.println("Fetching image from S3 for id: " + id);
        Outfit outfit = outfitRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Not Found: " + id));
        if (!outfit.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 권한이 없을 경우
        }
        try {
            InputStream inputStream = s3Client.getObject(BUCKET_NAME, outfit.getFileKey()).getObjectContent();
            byte[] imageBytes = inputStream.readAllBytes();
            ByteArrayResource resource = new ByteArrayResource(imageBytes);

            HttpHeaders headers = new HttpHeaders();
            String encodedFileName = URLEncoder.encode(outfit.getFileName(), StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + encodedFileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(imageBytes.length)
                    .contentType(MediaType.IMAGE_JPEG) // 이미지 타입에 맞게 설정
                    .body(resource);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{category}")
    public ResponseEntity<List<Outfit>> getUserOutfitList(@PathVariable String category, @AuthenticationPrincipal User user) {
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        List<Outfit> outfitList;

        if ("All".equalsIgnoreCase(category)) {
            // 카테고리가 "All"일 경우: 유저의 모든 옷 정보 반환
            outfitList = outfitRepository.findAllByUserId(user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("No outfits found for user: " + user.getId()));
        } else if (OutfitCategory.contains(category)) {
            // 특정 카테고리에 대한 옷 정보 반환
            OutfitCategory outfitCategory = OutfitCategory.valueOf(category.toUpperCase()); // 문자열을 Enum으로 변환
            outfitList = outfitRepository.findAllByUserIdAndCategory(user.getId(), outfitCategory)
                    .orElseThrow(() -> new IllegalArgumentException("No outfits found for user: " + user.getId() + " and category: " + category));
        } else {
            // 카테고리가 유효하지 않은 경우 예외 처리
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(outfitList, HttpStatus.OK);
    }

    @DeleteMapping("/image/{id}")
    public ResponseEntity<String> deleteImageFromS3(@PathVariable Long id, @AuthenticationPrincipal User user) {
        Outfit outfit = outfitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Not Found: " + id));

        if (!outfit.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();  // 권한이 없을 때
        }

        try {
            DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(BUCKET_NAME, outfit.getFileName());
            s3Client.deleteObject(deleteObjectRequest);
            outfitRepository.delete(outfit);

            System.out.println("File deleted successfully: " + outfit.getFileName());
            return ResponseEntity.ok().body(outfit.getFileName() + " Delete Complete!");

        } catch (AmazonS3Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("S3 Service Error: " + e.getMessage());

        } catch (SdkClientException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("SDK Client Error: " + e.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unknown Error: " + e.getMessage());
        }
    }

    @PutMapping("/image/{id}")
    public ResponseEntity<String> updateOutfitImage(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("category") OutfitCategory category,
            @RequestParam("folder") String folder,
            @RequestParam("description") String description) {

        // Find the outfit by ID
        Outfit outfit = outfitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Not Found: " + id));

        // Check if the authenticated user is the owner of the outfit
        if (!outfit.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // If a new file is provided, delete the existing image and upload the new one
        if (file != null) {
            try {
                // Delete the existing image from S3
                DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(BUCKET_NAME, outfit.getFileKey());
                s3Client.deleteObject(deleteObjectRequest);
            } catch (AmazonS3Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting old image from S3: " + e.getMessage());
            }

            String newFileKey = System.currentTimeMillis() + "_" + user.getId() + "_" + file.getOriginalFilename();
            String newFileName = file.getOriginalFilename();
            File tempFile = convertMultiPartToFile(file);
            try {
                s3Client.putObject(new PutObjectRequest(BUCKET_NAME, newFileKey, tempFile));
                outfit.setFileKey(newFileKey);
                outfit.setFileName(newFileName);
                outfit.setImageUrl(s3Client.getUrl(BUCKET_NAME, newFileKey).toString());
            } catch (SdkClientException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading new image to S3: " + e.getMessage());
            } finally {
                tempFile.delete(); // Delete the temporary file after upload
            }
        }

        // Update the outfit metadata in the database (even if no new image is provided)
        outfit.setCategory(category);
        outfit.setFolder(folder);
        outfit.setDescription(description);

        outfitRepository.save(outfit); // Save the updated outfit

        return ResponseEntity.ok("Outfit updated successfully!");
    }
}
