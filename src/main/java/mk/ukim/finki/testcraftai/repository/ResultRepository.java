package mk.ukim.finki.testcraftai.repository;

import mk.ukim.finki.testcraftai.model.Quiz;
import mk.ukim.finki.testcraftai.model.Result;
import mk.ukim.finki.testcraftai.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResultRepository extends JpaRepository<Result, Long> {
    List<Result> findByStudent(User student);

    // Add support for pagination to fetch recent results
    List<Result> findByStudent(User student, Pageable pageable);

    /**
     * Finds all results by quiz and student.
     *
     * @param quiz the quiz
     * @param student the student
     * @return list of results
     */
    List<Result> findByQuizAndStudent(Quiz quiz, User student);
}
