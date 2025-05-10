package mk.ukim.finki.testcraftai.repository;

import mk.ukim.finki.testcraftai.model.Quiz;
import mk.ukim.finki.testcraftai.model.Subject;
import mk.ukim.finki.testcraftai.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    Page<Quiz> findByUser(User user, Pageable pageable);
    Page<Quiz> findBySubject(Subject subject, Pageable pageable);
}
