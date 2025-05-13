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

            content.setFont(PDType1Font.HELVETICA_BOLD, 16);
            content.beginText();
            content.newLineAtOffset(margin, y);
            content.showText("Quiz: " + quiz.getTitle());
            content.endText();

            y -= 25;
            if (quiz.getDescription() != null) {
                content.setFont(PDType1Font.HELVETICA, 12);
                content.beginText();
                content.newLineAtOffset(margin, y);
                content.showText("Description: " + quiz.getDescription());
                content.endText();
                y -= 20;
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
                content.beginText();
                content.newLineAtOffset(margin, y);
                content.showText(questionNumber + ". " + question.getQuestionText());
                content.endText();
                y -= 18;

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

                    content.beginText();
                    content.newLineAtOffset(margin + 20, y);
                    String line = optionLabel + ") " + option.getOptionText();
                    content.showText(line);
                    content.endText();
                    y -= 15;
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
}
