package com.hqc.hophuddles.controller;

import com.hqc.hophuddles.service.FileStorageService;
import com.hqc.hophuddles.service.PDFGenerationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/files")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileStorageService fileStorageService;
    private final PDFGenerationService pdfGenerationService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "general") String category) {

        try {
            String fileName = fileStorageService.storeFile(file, category);
            String fileUrl = fileStorageService.getFileUrl(fileName);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "File uploaded successfully");
            response.put("fileName", fileName);
            response.put("fileUrl", fileUrl);
            response.put("originalName", file.getOriginalFilename());
            response.put("size", file.getSize());
            response.put("contentType", file.getContentType());
            response.put("category", category);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("File upload failed", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/upload-multiple")
    public ResponseEntity<Map<String, Object>> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "category", defaultValue = "general") String category) {

        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> uploadedFiles = new HashMap<>();

            for (MultipartFile file : files) {
                String fileName = fileStorageService.storeFile(file, category);
                String fileUrl = fileStorageService.getFileUrl(fileName);

                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("fileName", fileName);
                fileInfo.put("fileUrl", fileUrl);
                fileInfo.put("originalName", file.getOriginalFilename());
                fileInfo.put("size", file.getSize());

                uploadedFiles.put(file.getOriginalFilename(), fileInfo);
            }

            response.put("success", true);
            response.put("message", "Files uploaded successfully");
            response.put("files", uploadedFiles);
            response.put("count", files.length);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Multiple file upload failed", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        try {
            Resource resource = fileStorageService.loadFileAsResource(fileName);

            // Try to determine file's content type
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                log.info("Could not determine file type.");
            }

            // Fallback to the default content type if type could not be determined
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("File download failed for: " + fileName, e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{fileName:.+}")
    public ResponseEntity<Map<String, Object>> deleteFile(@PathVariable String fileName) {
        try {
            boolean deleted = fileStorageService.deleteFile(fileName);

            Map<String, Object> response = new HashMap<>();
            if (deleted) {
                response.put("success", true);
                response.put("message", "File deleted successfully");
            } else {
                response.put("success", false);
                response.put("message", "File not found or could not be deleted");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("File deletion failed for: " + fileName, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/generate-pdf")
    public ResponseEntity<Map<String, Object>> generatePdf(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(value = "category", defaultValue = "generated") String category) {

        try {
            String pdfPath = pdfGenerationService.generateSimplePdf(content, title, category);
            String fileUrl = fileStorageService.getFileUrl(pdfPath);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "PDF generated successfully");
            response.put("fileName", pdfPath);
            response.put("fileUrl", fileUrl);
            response.put("title", title);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("PDF generation failed", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/info/{fileName:.+}")
    public ResponseEntity<Map<String, Object>> getFileInfo(@PathVariable String fileName) {
        try {
            Resource resource = fileStorageService.loadFileAsResource(fileName);

            Map<String, Object> response = new HashMap<>();
            response.put("fileName", fileName);
            response.put("exists", resource.exists());
            response.put("readable", resource.isReadable());
            response.put("size", resource.contentLength());
            response.put("fileUrl", fileStorageService.getFileUrl(fileName));

            // File type detection
            response.put("isImage", fileStorageService.isImageFile(fileName));
            response.put("isDocument", fileStorageService.isDocumentFile(fileName));
            response.put("isAudio", fileStorageService.isAudioFile(fileName));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("File info retrieval failed for: " + fileName, e);
            return ResponseEntity.notFound().build();
        }
    }
}