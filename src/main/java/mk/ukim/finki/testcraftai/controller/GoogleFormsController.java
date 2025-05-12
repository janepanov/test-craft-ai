package mk.ukim.finki.testcraftai.controller;
import lombok.RequiredArgsConstructor;
import mk.ukim.finki.testcraftai.model.Question;
import mk.ukim.finki.testcraftai.model.Quiz;
import mk.ukim.finki.testcraftai.repository.QuizRepository;
import mk.ukim.finki.testcraftai.service.GoogleFormsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class GoogleFormsController {

    private final QuizRepository quizRepository;
    private final GoogleFormsService googleFormsService;

    @GetMapping("/export-form")
    public String exportForm(@RequestParam("quizId") Long quizId, Model model) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow();
        List<String> questionTexts = quiz.getQuestions()
                .stream().map(Question::getQuestionText).collect(Collectors.toList());

        String formUrl = googleFormsService.createFormFromQuiz(quiz.getTitle(), questionTexts);

        quiz.setGoogleFormsUrl(formUrl);
        quizRepository.save(quiz);

        model.addAttribute("link", formUrl);
        return "export-success";
    }
}
