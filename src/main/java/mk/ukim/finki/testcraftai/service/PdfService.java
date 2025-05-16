package mk.ukim.finki.testcraftai.service;

import lombok.AllArgsConstructor;
import mk.ukim.finki.testcraftai.model.Option;
import mk.ukim.finki.testcraftai.model.Question;
import mk.ukim.finki.testcraftai.model.Quiz;
import mk.ukim.finki.testcraftai.repository.QuizRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class PdfService {
    private final QuizRepository quizRepository;

    @Transactional
    public byte[] generateQuizPdf(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() ->
                new IllegalArgumentException("Quiz not found with ID: " + quizId));

        // Ensure questions and options are loaded
        quiz.getQuestions().forEach(q -> q.getOptions().size());

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // Load a built-in font that supports Unicode
            InputStream fontStream = PdfService.class.getResourceAsStream("/fonts/NotoSans-Regular.ttf");
            if (fontStream == null) {
                throw new RuntimeException("Font file not found in resources");
            }
            PDType0Font font = PDType0Font.load(document, fontStream);

            float y = 750;
            float margin = 50;
            float maxWidth = PDRectangle.A4.getWidth() - 2 * margin;
            float lineHeight = 15;

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            try {
                contentStream.setFont(font, 16);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, y);
                contentStream.showText("Quiz: " + quiz.getTitle());
                contentStream.endText();

                y -= 25;

                if (quiz.getDescription() != null) {
                    contentStream.setFont(font, 12);
                    y = writeWrappedText(
                            contentStream,
                            "Description: " + quiz.getDescription(),
                            margin,
                            y,
                            maxWidth,
                            lineHeight,
                            font,
                            12);
                }

                int questionNumber = 1;
                for (Question question : quiz.getQuestions()) {
                    if (y < 100) {
                        contentStream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        y = 750;
                    }

                    contentStream.setFont(font, 12);
                    y = writeWrappedText(
                            contentStream,
                            questionNumber + ". " + question.getQuestionText(),
                            margin,
                            y - 10,
                            maxWidth,
                            lineHeight,
                            font,
                            12);

                    char optionLabel = 'A';
                    for (Option option : question.getOptions()) {
                        if (y < 80) {
                            contentStream.close();
                            page = new PDPage(PDRectangle.A4);
                            document.addPage(page);
                            contentStream = new PDPageContentStream(document, page);
                            y = 750;
                        }

                        y = writeWrappedText(
                                contentStream,
                                optionLabel + ") " + option.getOptionText(),
                                margin + 20,
                                y - 5,
                                maxWidth - 20,
                                lineHeight,
                                font,
                                11);
                        optionLabel++;
                    }

                    y -= 10;
                    questionNumber++;
                }
            } finally {
                contentStream.close();
            }

            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private float writeWrappedText(PDPageContentStream contentStream, String text, float x, float y, float maxWidth, float lineHeight, PDType0Font font, int fontSize) throws IOException {
        contentStream.setFont(font, fontSize);
        List<String> lines = wrapText(text, maxWidth, font, fontSize);
        for (String line : lines) {
            if (y < 50) {
                throw new IOException("Page overflow detected. Add logic to handle new pages.");
            }
            contentStream.beginText();
            contentStream.newLineAtOffset(x, y);
            contentStream.showText(line);
            contentStream.endText();
            y -= lineHeight;
        }
        return y;
    }

    private List<String> wrapText(String text, float maxWidth, PDType0Font font, int fontSize) throws IOException {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        for (String word : words) {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            float textWidth = font.getStringWidth(testLine) / 1000 * fontSize;
            if (textWidth > maxWidth) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                currentLine.append(currentLine.isEmpty() ? word : " " + word);
            }
        }
        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }
        return lines;
    }
}
