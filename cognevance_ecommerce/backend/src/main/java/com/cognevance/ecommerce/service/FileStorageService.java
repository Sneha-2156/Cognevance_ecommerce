package com.cognevance.ecommerce.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Stores uploaded product images on local disk under app.upload-dir and
 * returns a public /uploads/<filename> URL (served by WebConfig).
 *
 * For real cloud storage (AWS S3, Cloudinary, etc.), swap the body of
 * store() for an SDK upload call and return the returned CDN URL instead -
 * the rest of the app (ProductController, frontend) doesn't need to change,
 * since it only ever deals with the resulting imageUrl string.
 */
@Service
public class FileStorageService {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload an empty file");
        }

        String original = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
        String extension = original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
        String filename = UUID.randomUUID() + extension;

        try {
            Path dirPath = Paths.get(uploadDir);
            Files.createDirectories(dirPath);
            Path target = dirPath.resolve(filename);
            Files.copy(file.getInputStream(), target);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }

        return "/uploads/" + filename;
    }
}
