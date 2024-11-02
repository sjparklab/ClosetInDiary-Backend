package me.parkseongjong.springbootdeveloper.controller;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import me.parkseongjong.springbootdeveloper.domain.Outfit;
import me.parkseongjong.springbootdeveloper.domain.User;
import me.parkseongjong.springbootdeveloper.repository.OutfitRepository;
import me.parkseongjong.springbootdeveloper.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/outfits")
public class OutfitController {

    @Autowired
    private AmazonS3 s3Client;

    @Autowired
    private OutfitRepository outfitRepository;

    @Autowired
    private UserRepository userRepository;

    private static final String BUCKET_NAME = "closetindiary-image-bucket";

    @PostMapping("/upload")
    public ResponseEntity<String> uploadOutfitImage(@RequestParam("file") MultipartFile file,
                                                    @RequestParam("category") String category,
                                                    @RequestParam("folder") String folder,
                                                    @RequestParam("description") String description,
                                                    @AuthenticationPrincipal User user) {
        if (user == null) {
            return new ResponseEntity<>("User not authenticated", HttpStatus.UNAUTHORIZED);
        }
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        File tempFile = convertMultiPartToFile(file);
        s3Client.putObject(new PutObjectRequest(BUCKET_NAME, fileName, tempFile));
        String imageUrl = s3Client.getUrl(BUCKET_NAME, fileName).toString();

        // Save outfit metadata to the database
        Outfit outfit = Outfit.builder()
                .user(user)
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

    @GetMapping("/image/{id}")
    public ResponseEntity<Resource> getImageFromS3(@PathVariable Long id, @AuthenticationPrincipal User user) {
        Outfit outfit = outfitRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Not Found: " + id));
        if (!outfit.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 권한이 없을 경우
        }
        try {
            InputStream inputStream = s3Client.getObject(BUCKET_NAME, outfit.getFileName()).getObjectContent();
            byte[] imageBytes = inputStream.readAllBytes();
            ByteArrayResource resource = new ByteArrayResource(imageBytes);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + outfit.getFileName());

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

    @GetMapping("/list")
    public ResponseEntity<List<Outfit>> getUserOutfitList(@AuthenticationPrincipal User user) {
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        List<Outfit> outfitList = outfitRepository.findAllByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Not Found for user: " + user.getId()));

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
}
