package mk.ukim.finki.testcraftai.controller;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.testcraftai.model.Quiz;
import mk.ukim.finki.testcraftai.model.Result;
import mk.ukim.finki.testcraftai.model.User;
import mk.ukim.finki.testcraftai.service.QuizService;
import mk.ukim.finki.testcraftai.service.ResultService;
import mk.ukim.finki.testcraftai.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class StudentController {

    private final QuizService quizService;
    private final ResultService resultService;
    private final UserService userService;

    @GetMapping("/student/dashboard")
    public String getStudentDashboard(Authentication authentication, Model model) {
        String username = authentication.getName();

        User user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        List<Quiz> availableQuizzes = quizService.getAllAvailableQuizzes();
        List<Result> completedQuizzes = resultService.getResultsByStudent(username);
        List<Quiz> recentQuizzes = quizService.getRecentQuizzes();
        List<Result> recentResults = resultService.getRecentResultsByStudent(username);

        model.addAttribute("user", user);
        model.addAttribute("availableQuizzes", availableQuizzes);
        model.addAttribute("completedQuizzes", completedQuizzes);
        model.addAttribute("recentQuizzes", recentQuizzes);
        model.addAttribute("recentResults", recentResults);

        return "student/dashboard";
    }

    @GetMapping("/student/quizzes")
    public String getAvailableQuizzes(Model model) {
        List<Quiz> quizzes = quizService.getAllAvailableQuizzes();
        model.addAttribute("quizzes", quizzes);
        return "student/available-quizzes";
    }

    @GetMapping("/student/quizzes/{id}")
    public String getQuizForStudent(@PathVariable Long id, Model model) {
        Quiz quiz = quizService.getQuizById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found with ID: " + id));
        model.addAttribute("quiz", quiz);
        return "student/take-quiz";
    }

    @PostMapping("/student/quizzes/{id}/submit")
    public String submitQuiz(
            @PathVariable Long id,
            @RequestParam List<Long> answers,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        int score = quizService.calculateQuizScore(id, answers, username);
        redirectAttributes.addFlashAttribute("score", score);
        return "redirect:/student/quizzes/" + id + "/result";
    }

    @GetMapping("/student/quizzes/{id}/result")
    public String getQuizResult(@PathVariable Long id, Authentication authentication, Model model) {
        String username = authentication.getName();

        // Retrieve the quiz
        Quiz quiz = quizService.getQuizById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found with ID: " + id));

        // Retrieve the result for the quiz and the authenticated user
        Result result = resultService.getResultByQuizAndStudent(quiz, username)
                .orElseThrow(() -> new IllegalArgumentException("Result not found for quiz ID: " + id));

        // Add quiz and score to the model
        model.addAttribute("quiz", quiz);
        model.addAttribute("score", result.getScore());

        return "student/quiz-result";
    }

    @GetMapping("/student/results")
    public String getStudentResults(Authentication authentication, Model model) {
        String username = authentication.getName();
        List<Result> results = resultService.getResultsByStudent(username);
        model.addAttribute("results", results);
        return "student/results";
    }
}
