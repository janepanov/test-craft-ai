package mk.ukim.finki.testcraftai.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionOption {
    private String text;
    private boolean isCorrect;

    public QuestionOption(String text, boolean isCorrect) {
        this.text = text;
        this.isCorrect = isCorrect;
    }
}
