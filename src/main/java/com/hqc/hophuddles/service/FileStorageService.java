package com.hqc.hophuddles.service;

import com.hqc.hophuddles.exception.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    private final Path fileStorageLocation;
    private final long maxFileSize;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp");
    private static final List<String> ALLOWED_DOCUMENT_TYPES = Arrays.asList("pdf", "doc", "docx", "txt");
    private static final List<String> ALLOWED_AUDIO_TYPES = Arrays.asList("mp3", "wav", "m4a", "aac");

    public FileStorageService(@Value("${app.file.upload-dir}") String uploadDir,
                              @Value("${app.file.max-size}") long maxFileSize) {
        this.maxFileSize = maxFileSize;
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file, String category) {
        validateFile(file);

        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = FilenameUtils.getExtension(fileName);

        // Generate unique filename
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uniqueFileName = String.format("%s_%s_%s.%s",
                category, timestamp, UUID.randomUUID().toString().substring(0, 8), fileExtension);

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Create category directory if it doesn't exist
            Path categoryPath = this.fileStorageLocation.resolve(category);
            Files.createDirectories(categoryPath);

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = categoryPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("File stored successfully: {}", targetLocation);
            return category + "/" + uniqueFileName;

        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new FileStorageException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new FileStorageException("File not found " + fileName, ex);
        }
    }

    public boolean deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            return Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            log.error("Could not delete file " + fileName, ex);
            return false;
        }
    }

    public String getFileUrl(String fileName) {
        return "/files/" + fileName;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException("Cannot store empty file");
        }

        if (file.getSize() > maxFileSize) {
            throw new FileStorageException("File size exceeds maximum allowed size of " + maxFileSize + " bytes");
        }

        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = FilenameUtils.getExtension(fileName).toLowerCase();

        List<String> allAllowedTypes = Arrays.asList();
        allAllowedTypes.addAll(ALLOWED_IMAGE_TYPES);
        allAllowedTypes.addAll(ALLOWED_DOCUMENT_TYPES);
        allAllowedTypes.addAll(ALLOWED_AUDIO_TYPES);

        if (!allAllowedTypes.contains(fileExtension)) {
            throw new FileStorageException("File type not allowed: " + fileExtension);
        }
    }

    public boolean isImageFile(String fileName) {
        String extension = FilenameUtils.getExtension(fileName).toLowerCase();
        return ALLOWED_IMAGE_TYPES.contains(extension);
    }

    public boolean isDocumentFile(String fileName) {
        String extension = FilenameUtils.getExtension(fileName).toLowerCase();
        return ALLOWED_DOCUMENT_TYPES.contains(extension);
    }

    public boolean isAudioFile(String fileName) {
        String extension = FilenameUtils.getExtension(fileName).toLowerCase();
        return ALLOWED_AUDIO_TYPES.contains(extension);
    }
}