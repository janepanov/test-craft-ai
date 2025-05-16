package mk.ukim.finki.testcraftai.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import mk.ukim.finki.testcraftai.model.Material;
import mk.ukim.finki.testcraftai.model.Question;
import mk.ukim.finki.testcraftai.model.Quiz;
import mk.ukim.finki.testcraftai.model.Subject;
import mk.ukim.finki.testcraftai.model.dto.QuizGenerationRequest;
import mk.ukim.finki.testcraftai.service.MaterialService;
import mk.ukim.finki.testcraftai.service.QuizService;
import mk.ukim.finki.testcraftai.service.SubjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final MaterialService materialService;
    private final SubjectService subjectService;

    @GetMapping("/teacher/quizzes")
    public String getQuizzesPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication,
            Model model) {

        Page<Quiz> quizzes = quizService.getQuizzesByUser(
                authentication.getName(),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        model.addAttribute("quizzes", quizzes);
        return "teacher/quizzes";
    }

    @GetMapping("/teacher/quizzes/create")
    public String getCreateQuizPage(Model model) {
        List<Material> materials = materialService.getAllMaterials();
        List<Subject> subjects = subjectService.getAllSubjects();
        List<Question.QuestionType> questionTypes = Arrays.asList(Question.QuestionType.values());

        model.addAttribute("materials", materials);
        model.addAttribute("subjects", subjects);
        model.addAttribute("questionTypes", questionTypes);
        return "teacher/create-quiz";
    }

    @PostMapping("/teacher/quizzes/create")
    public String createQuiz(
            @RequestParam("materialId") Long materialId,
            @RequestParam("subjectId") Long subjectId,
            @RequestParam("numberOfQuestions") int numberOfQuestions,
            @RequestParam("questionTypes") List<Question.QuestionType> questionTypes,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            Material material = materialService.getMaterialById(materialId)
                    .orElseThrow(() -> new IllegalArgumentException("Material not found with ID: " + materialId));

            QuizGenerationRequest request = new QuizGenerationRequest();
            request.setSubjectId(subjectId);
            request.setNumberOfQuestions(numberOfQuestions);
            request.setQuestionTypes(questionTypes);

            Quiz quiz = quizService.generateQuiz(material.getContent(), request, authentication.getName());

            redirectAttributes.addFlashAttribute("success",
                    "Quiz '" + quiz.getTitle() + "' created successfully!");
            return "redirect:/teacher/quizzes";
        } catch (JsonProcessingException e) {
            redirectAttributes.addFlashAttribute("error",
                    "Failed to create quiz: " + e.getMessage());
            return "redirect:/teacher/quizzes/create";
        }
    }

    @GetMapping("/teacher/quizzes/{id}")
    public String getQuizDetails(@PathVariable Long id, Model model) {
        Quiz quiz = quizService.getQuizById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found with ID: " + id));

        model.addAttribute("quiz", quiz);
        return "teacher/quiz-details";
    }
}
