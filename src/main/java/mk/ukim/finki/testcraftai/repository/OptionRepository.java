package mk.ukim.finki.testcraftai.repository;

import mk.ukim.finki.testcraftai.model.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OptionRepository extends JpaRepository<Option, Long> {
}
