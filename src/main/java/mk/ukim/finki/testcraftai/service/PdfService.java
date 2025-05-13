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
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

        PDDocument document = new PDDocument();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PDPageContentStream content = null;

        try {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            content = new PDPageContentStream(document, page);
            float y = 750;
            float margin = 50;
            float maxWidth = PDRectangle.A4.getWidth() - 2 * margin;
            float lineHeight = 15;

            content.setFont(PDType1Font.HELVETICA_BOLD, 16);
            content.beginText();
            content.newLineAtOffset(margin, y);
            content.showText("Quiz: " + quiz.getTitle());
            content.endText();

            y -= 25;
            if (quiz.getDescription() != null) {
                content.setFont(PDType1Font.HELVETICA, 12);
                y = writeWrappedText(
                        content,
                        "Description: " + quiz.getDescription(),
                        margin,
                        y,
                        maxWidth,
                        lineHeight);

            }

            int questionNumber = 1;
            for (Question question : quiz.getQuestions()) {
                if (y < 100) {
                    content.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    content = new PDPageContentStream(document, page);
                    y = 750;
                }

                content.setFont(PDType1Font.HELVETICA_BOLD, 12);
                y = writeWrappedText(
                        content,
                        questionNumber + ". " + question.getQuestionText(),
                        margin,
                        y - 10,
                        maxWidth,
                        lineHeight);

                content.setFont(PDType1Font.HELVETICA, 11);
                char optionLabel = 'A';
                for (Option option : question.getOptions()) {
                    if (y < 80) {
                        content.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        content = new PDPageContentStream(document, page);
                        y = 750;
                    }

                    y = writeWrappedText(
                            content,
                            optionLabel + ") " + option.getOptionText(),
                            margin + 20,
                            y - 5,
                            maxWidth - 20,
                            lineHeight);
                    optionLabel++;
                }

                y -= 10;
                questionNumber++;
            }

            content.close();
            document.save(out);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle logging or throw custom exception
        } finally {
            try {
                if (content != null) content.close();
                document.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return out.toByteArray();
    }

    private float writeWrappedText(PDPageContentStream contentStream, String text, float x, float y, float maxWidth, float lineHeight) throws IOException {
        List<String> lines = wrapText(text, maxWidth);
        for (String line : lines) {
            if (y < 50) {
                contentStream.close();
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

    private List<String> wrapText(String text, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            float textWidth = PDType1Font.HELVETICA.getStringWidth(testLine) / 1000 * 12; // Font size 12
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
