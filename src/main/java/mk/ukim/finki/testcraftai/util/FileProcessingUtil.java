package mk.ukim.finki.testcraftai.util;

import mk.ukim.finki.testcraftai.model.Material;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Utility class for processing files in various formats.
 */
public final class FileProcessingUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileProcessingUtil.class);

    private static final String CONTENT_TYPE_PDF = "application/pdf";
    private static final String CONTENT_TYPE_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    private static final String EXTENSION_PDF = ".pdf";
    private static final String EXTENSION_DOCX = ".docx";
    private static final String EXTENSION_TXT = ".txt";

    private static final float MARGIN = 50;
    private static final float FONT_SIZE = 12;
    private static final float LEADING = 14.5f;
    private static final PDType1Font FONT = PDType1Font.HELVETICA;

    private FileProcessingUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static String extractTextFromFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        String contentType = file.getContentType();

        if (contentType == null) {
            logger.warn("Content type is null, treating as plain text");
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        }

        return switch (contentType) {
            case CONTENT_TYPE_PDF -> extractTextFromPdf(file);
            case CONTENT_TYPE_DOCX -> extractTextFromDocx(file);
            default -> {
                logger.info("Unrecognized content type: {}, treating as plain text", contentType);
                yield new String(file.getBytes(), StandardCharsets.UTF_8);
            }
        };
    }

    private static String extractTextFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private static String extractTextFromDocx(MultipartFile file) throws IOException {
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            return extractor.getText();
        }
    }

    public static Material.FileType determineFileType(String fileName) {
        if (fileName == null) {
            return Material.FileType.TXT;
        }

        String lowerCaseFileName = fileName.toLowerCase();

        if (lowerCaseFileName.endsWith(EXTENSION_PDF)) {
            return Material.FileType.PDF;
        } else if (lowerCaseFileName.endsWith(EXTENSION_DOCX)) {
            return Material.FileType.DOCX;
        } else if (lowerCaseFileName.endsWith(EXTENSION_TXT)) {
            return Material.FileType.TXT;
        } else {
            logger.warn("Unrecognized file extension for: {}, defaulting to TXT", fileName);
            return Material.FileType.TXT;
        }
    }

    public static byte[] generatePdfFromText(String content) throws IOException {
        Objects.requireNonNull(content, "Content cannot be null");

        try (PDDocument pdfDocument = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.LETTER);
            pdfDocument.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(pdfDocument, page)) {
                contentStream.setFont(FONT, FONT_SIZE);
                contentStream.setLeading(LEADING);
                contentStream.beginText();
                contentStream.newLineAtOffset(MARGIN, PDRectangle.LETTER.getHeight() - MARGIN);

                float width = PDRectangle.LETTER.getWidth() - 2 * MARGIN;
                String[] words = content.split("\\s+");
                StringBuilder line = new StringBuilder();

                for (String word : words) {
                    String testLine = line + word + " ";
                    float size = FONT.getStringWidth(testLine) / 1000 * FONT_SIZE;

                    if (size > width) {
                        contentStream.showText(line.toString().trim());
                        contentStream.newLine();
                        line = new StringBuilder(word + " ");
                    } else {
                        line.append(word).append(" ");
                    }
                }

                if (!line.isEmpty()) {
                    contentStream.showText(line.toString().trim());
                }

                contentStream.endText();
            }

            pdfDocument.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    public static byte[] generateDocxFromText(String content) throws IOException {
        Objects.requireNonNull(content, "Content cannot be null");

        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();

            run.setFontSize(12);
            run.setFontFamily("Helvetica");

            String[] paragraphs = content.split("\n");
            for (int i = 0; i < paragraphs.length; i++) {
                run.setText(paragraphs[i]);
                if (i < paragraphs.length - 1) {
                    run.addBreak();
                }
            }

            document.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public static byte[] generateTxtFromText(String content) {
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null");
        }
        return content.getBytes(StandardCharsets.UTF_8);
    }
}