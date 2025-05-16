package mk.ukim.finki.testcraftai.controller;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.testcraftai.model.Question;
import mk.ukim.finki.testcraftai.model.Quiz;
import mk.ukim.finki.testcraftai.model.dto.QuestionOption;
import mk.ukim.finki.testcraftai.repository.QuizRepository;
import mk.ukim.finki.testcraftai.service.GoogleFormsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class GoogleFormsController {

    private final QuizRepository quizRepository;
    private final GoogleFormsService googleFormsService;

    @GetMapping("/export-form")
    public String exportForm(@RequestParam("quizId") Long quizId, Model model) throws IOException {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found with ID: " + quizId));

        // Create a map of questions and their options with correct answer metadata
        Map<String, List<QuestionOption>> questionsWithOptions = quiz.getQuestions().stream()
                .collect(Collectors.toMap(
                        Question::getQuestionText,
                        question -> question.getOptions().stream()
                                .map(option -> new QuestionOption(option.getOptionText(), option.isCorrect()))
                                .toList()
                ));

        // Call the service to create the Google Form
        String formUrl = googleFormsService.createFormFromQuiz(quiz.getTitle(), questionsWithOptions);

        // Save the form URL to the quiz and update the database
        quiz.setGoogleFormsUrl(formUrl);
        quizRepository.save(quiz);

        // Add the form URL to the model for the view
        model.addAttribute("link", formUrl);
        return "export-success";
    }
}
