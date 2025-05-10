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

    // Content type constants
    private static final String CONTENT_TYPE_PDF = "application/pdf";
    private static final String CONTENT_TYPE_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    // File extension constants
    private static final String EXTENSION_PDF = ".pdf";
    private static final String EXTENSION_DOCX = ".docx";
    private static final String EXTENSION_TXT = ".txt";

    // PDF generation constants
    private static final float MARGIN = 50;
    private static final float FONT_SIZE = 12;
    private static final float LEADING = 14.5f;
    private static final PDType1Font FONT = PDType1Font.HELVETICA;

    // Private constructor to prevent instantiation
    private FileProcessingUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Extracts text content from various file types (PDF, DOCX, TXT)
     *
     * @param file the uploaded file
     * @return extracted text content
     * @throws IOException if file processing fails
     * @throws IllegalArgumentException if file is null or empty
     */
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

    /**
     * Extracts text from a PDF file
     *
     * @param file the PDF file
     * @return extracted text
     * @throws IOException if PDF processing fails
     */
    private static String extractTextFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * Extracts text from a DOCX file
     *
     * @param file the DOCX file
     * @return extracted text
     * @throws IOException if DOCX processing fails
     */
    private static String extractTextFromDocx(MultipartFile file) throws IOException {
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            return extractor.getText();
        }
    }

    /**
     * Determines the file type from the file name
     *
     * @param fileName the name of the file
     * @return the corresponding Material.FileType
     */
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

    /**
     * Generates a PDF file from text content
     *
     * @param content the text content to convert to PDF
     * @return byte array of the PDF file
     * @throws IOException if PDF generation fails
     */
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

    /**
     * Generates a DOCX file from text content
     *
     * @param content the text content to convert to DOCX
     * @return byte array of the DOCX file
     * @throws IOException if DOCX generation fails
     */
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

    /**
     * Generates a TXT file from text content
     *
     * @param content the text content to convert to TXT
     * @return byte array of the TXT file
     * @throws IllegalArgumentException if content is null
     */
    public static byte[] generateTxtFromText(String content) {
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null");
        }
        return content.getBytes(StandardCharsets.UTF_8);
    }
}