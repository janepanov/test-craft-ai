package mk.ukim.finki.testcraftai.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mk.ukim.finki.testcraftai.model.Question;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizGenerationRequest {
    private String title;
    private Long subjectId;
    private int numberOfQuestions = 10;
    private List<Question.QuestionType> questionTypes = List.of(Question.QuestionType.MULTIPLE_CHOICE, Question.QuestionType.TRUE_FALSE);
}
