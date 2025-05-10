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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /**
     * Generates a quiz from material content
     *
     * @param materialContent the educational content
     * @param request quiz generation parameters
     * @param username the username of the creator
     * @return the created quiz
     * @throws JsonProcessingException if JSON processing fails
     */
    @Transactional
    public Quiz generateQuiz(String materialContent, QuizGenerationRequest request, String username)
            throws JsonProcessingException {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with ID: " + request.getSubjectId()));

        // Generate quiz questions using OpenAI
        QuizGenerationResponse response = openAIService.generateQuiz(materialContent, request);

        // Create the quiz
        Quiz quiz = new Quiz();
        quiz.setTitle(response.getTitle());
        quiz.setDescription("Generated from material content");
        quiz.setUser(user);
        quiz.setSubject(subject);
        quiz.setQuestions(new ArrayList<>());

        Quiz savedQuiz = quizRepository.save(quiz);

        // Create questions and options
        for (QuizGenerationResponse.QuestionDto questionDto : response.getQuestions()) {
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

    /**
     * Retrieves a quiz by ID
     *
     * @param id the quiz ID
     * @return optional containing the quiz if found
     */
    public Optional<Quiz> getQuizById(Long id) {
        return quizRepository.findById(id);
    }

    /**
     * Retrieves all quizzes for a user
     *
     * @param username the username
     * @param pageable pagination information
     * @return page of quizzes
     */
    public Page<Quiz> getQuizzesByUser(String username, Pageable pageable) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return quizRepository.findByUser(user, pageable);
    }

    /**
     * Retrieves all quizzes for a subject
     *
     * @param subjectId the subject ID
     * @param pageable pagination information
     * @return page of quizzes
     */
    public Page<Quiz> getQuizzesBySubject(Long subjectId, Pageable pageable) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with ID: " + subjectId));

        return quizRepository.findBySubject(subject, pageable);
    }

    /**
     * Retrieves all quizzes
     *
     * @return list of all quizzes
     */
    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }
}
