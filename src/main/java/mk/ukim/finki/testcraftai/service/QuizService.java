package mk.ukim.finki.testcraftai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import mk.ukim.finki.testcraftai.model.*;
import mk.ukim.finki.testcraftai.model.dto.QuizGenerationRequest;
import mk.ukim.finki.testcraftai.model.dto.QuizGenerationResponse;
import mk.ukim.finki.testcraftai.repository.OptionRepository;
import mk.ukim.finki.testcraftai.repository.QuestionRepository;
import mk.ukim.finki.testcraftai.repository.QuizRepository;
import mk.ukim.finki.testcraftai.repository.SubjectRepository;
import mk.ukim.finki.testcraftai.repository.ResultRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final SubjectRepository subjectRepository;
    private final UserService userService;
    private final OpenAIService openAIService;
    private final ResultRepository resultRepository;

    @Transactional
    public Quiz generateQuiz(String materialContent, QuizGenerationRequest request, String username)
            throws JsonProcessingException {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with ID: " + request.getSubjectId()));

        QuizGenerationResponse response = openAIService.generateQuiz(materialContent, request);

        List<QuizGenerationResponse.QuestionDto> filteredQuestions = response.getQuestions().stream()
                .filter(question -> request.getQuestionTypes().contains(question.getType()))
                .toList();

        Quiz quiz = new Quiz();
        quiz.setTitle(response.getTitle());
        quiz.setDescription("Generated from material content");
        quiz.setUser(user);
        quiz.setSubject(subject);
        quiz.setQuestions(new ArrayList<>());

        Quiz savedQuiz = quizRepository.save(quiz);

        for (QuizGenerationResponse.QuestionDto questionDto : filteredQuestions) {
            Question question = new Question();
            question.setQuestionText(questionDto.getQuestionText());
            question.setType(questionDto.getType());
            question.setQuiz(savedQuiz);
            question.setOptions(new ArrayList<>());

            Question savedQuestion = questionRepository.save(question);

            for (QuizGenerationResponse.OptionDto optionDto : questionDto.getOptions()) {
                Option option = new Option();
                option.setOptionText(optionDto.getOptionText());
                option.setCorrect(optionDto.isCorrect());
                option.setQuestion(savedQuestion);

                optionRepository.save(option);
                savedQuestion.getOptions().add(option);
            }

            savedQuiz.getQuestions().add(savedQuestion);
        }

        return savedQuiz;
    }

    public Optional<Quiz> getQuizById(Long id) {
        return quizRepository.findById(id);
    }

    public Page<Quiz> getQuizzesByUser(String username, Pageable pageable) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return quizRepository.findByUser(user, pageable);
    }

    public Page<Quiz> getQuizzesBySubject(Long subjectId, Pageable pageable) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with ID: " + subjectId));

        return quizRepository.findBySubject(subject, pageable);
    }

    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }

    public List<Quiz> getAllAvailableQuizzes() {
        return quizRepository.findAll();
    }

    @Transactional
    public int calculateQuizScore(Long quizId, List<Long> answers, String username) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found with ID: " + quizId));

        User student = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        int correctAnswers = 0;
        for (Question question : quiz.getQuestions()) {
            for (Option option : question.getOptions()) {
                if (option.isCorrect() && answers.contains(option.getId())) {
                    correctAnswers++;
                }
            }
        }

        Result result = new Result();
        result.setQuiz(quiz);
        result.setStudent(student);
        result.setScore(correctAnswers);
        resultRepository.save(result);

        return correctAnswers;
    }

    public List<Quiz> getRecentQuizzes() {
        return quizRepository.findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();
    }
}
