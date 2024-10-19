package me.parkseongjong.springbootdeveloper.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import me.parkseongjong.springbootdeveloper.domain.Outfit;
import me.parkseongjong.springbootdeveloper.domain.User;
import me.parkseongjong.springbootdeveloper.repository.OutfitRepository;
import me.parkseongjong.springbootdeveloper.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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

    @GetMapping("/image/{fileName}")
    public ResponseEntity<String> getImageUrl(@PathVariable String fileName) {
        String imageUrl = s3Client.getUrl(BUCKET_NAME, fileName).toString();
        return new ResponseEntity<>(imageUrl, HttpStatus.OK);
    }
}
