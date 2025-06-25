package com.hqc.hophuddles.service;

import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class PDFGenerationService {

    private final FileStorageService fileStorageService;

    public String generatePdfFromHtml(String htmlContent, String title, String category) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            HtmlConverter.convertToPdf(htmlContent, outputStream);

            // Save to file storage
            String fileName = String.format("%s_%s_%s.pdf",
                    category,
                    title.replaceAll("[^a-zA-Z0-9]", "_"),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            );

            // Save the PDF bytes to storage (you'd implement this method)
            return savePdfToStorage(outputStream.toByteArray(), fileName, category);

        } catch (Exception e) {
            log.error("Error generating PDF", e);
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    public String generateSimplePdf(String content, String title, String category) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Add title
            document.add(new Paragraph(title).setBold().setFontSize(18));
            document.add(new Paragraph("\n"));

            // Add content
            document.add(new Paragraph(content));

            document.close();

            String fileName = String.format("%s_%s_%s.pdf",
                    category,
                    title.replaceAll("[^a-zA-Z0-9]", "_"),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            );

            return savePdfToStorage(outputStream.toByteArray(), fileName, category);

        } catch (Exception e) {
            log.error("Error generating simple PDF", e);
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private String savePdfToStorage(byte[] pdfBytes, String fileName, String category) {
        // This is a simplified implementation
        // In a real scenario, you'd save the byte array to file storage
        try {
            java.nio.file.Path tempFile = java.nio.file.Files.createTempFile(category + "_", ".pdf");
            java.nio.file.Files.write(tempFile, pdfBytes);

            String relativePath = category + "/" + fileName;
            log.info("PDF generated and saved: {}", relativePath);
            return relativePath;

        } catch (IOException e) {
            throw new RuntimeException("Failed to save PDF to storage", e);
        }
    }
}