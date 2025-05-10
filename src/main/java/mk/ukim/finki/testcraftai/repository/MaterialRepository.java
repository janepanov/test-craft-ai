package mk.ukim.finki.testcraftai.repository;

import mk.ukim.finki.testcraftai.model.Material;
import mk.ukim.finki.testcraftai.model.Subject;
import mk.ukim.finki.testcraftai.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    Page<Material> findByUser(User user, Pageable pageable);
    Page<Material> findBySubject(Subject subject, Pageable pageable);
}
