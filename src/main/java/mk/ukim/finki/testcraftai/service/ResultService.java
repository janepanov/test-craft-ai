package mk.ukim.finki.testcraftai.service;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.testcraftai.model.Result;
import mk.ukim.finki.testcraftai.model.User;
import mk.ukim.finki.testcraftai.model.Quiz;
import mk.ukim.finki.testcraftai.repository.ResultRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResultService {

    private final ResultRepository resultRepository;
    private final UserService userService;

    public List<Result> getResultsByStudent(String username) {
        User student = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return resultRepository.findByStudent(student);
    }

    public List<Result> getRecentResultsByStudent(String username) {
        User student = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return resultRepository.findByStudent(student, PageRequest.of(0, 5));
    }

    public Result saveResult(Result result) {
        return resultRepository.save(result);
    }

    public Optional<Result> getResultByQuizAndStudent(Quiz quiz, String username) {
        User student = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        List<Result> results = resultRepository.findByQuizAndStudent(quiz, student);

        return results.stream()
                .max(Comparator.comparing(Result::getCreatedAt));
    }
}
