package mk.ukim.finki.testcraftai.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mk.ukim.finki.testcraftai.model.Question;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizGenerationResponse {
    private String title;
    private List<QuestionDto> questions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionDto {
        private String questionText;
        private Question.QuestionType type;
        private List<OptionDto> options;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionDto {
        private String optionText;
        private boolean isCorrect;
    }
}
