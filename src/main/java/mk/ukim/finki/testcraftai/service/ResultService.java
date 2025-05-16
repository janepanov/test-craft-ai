package mk.ukim.finki.testcraftai.service;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.testcraftai.model.Result;
import mk.ukim.finki.testcraftai.model.User;
import mk.ukim.finki.testcraftai.model.Quiz;
import mk.ukim.finki.testcraftai.repository.ResultRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResultService {

    private final ResultRepository resultRepository;
    private final UserService userService;

    /**
     * Retrieves all results for a specific student.
     *
     * @param username the username of the student
     * @return list of results
     */
    public List<Result> getResultsByStudent(String username) {
        User student = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return resultRepository.findByStudent(student);
    }

    /**
     * Retrieves the most recent results for a specific student.
     *
     * @param username the username of the student
     * @return list of recent results
     */
    public List<Result> getRecentResultsByStudent(String username) {
        User student = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return resultRepository.findByStudent(student, PageRequest.of(0, 5));
    }

    /**
     * Saves a result to the database.
     *
     * @param result the result to save
     * @return the saved result
     */
    public Result saveResult(Result result) {
        return resultRepository.save(result);
    }

    /**
     * Retrieves the result for a specific quiz and student.
     *
     * @param quiz the quiz
     * @param username the username of the student
     * @return optional containing the result if found
     */
    public Optional<Result> getResultByQuizAndStudent(Quiz quiz, String username) {
        User student = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return resultRepository.findByQuizAndStudent(quiz, student);
    }
}
