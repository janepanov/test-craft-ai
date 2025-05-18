package mk.ukim.finki.testcraftai.service;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.testcraftai.model.Subject;
import mk.ukim.finki.testcraftai.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;

    @Transactional
    public Subject createSubject(String name, String description) {
        if (subjectRepository.existsByName(name)) {
            throw new IllegalArgumentException("Subject with name '" + name + "' already exists");
        }

        Subject subject = new Subject();
        subject.setName(name);
        subject.setDescription(description);

        return subjectRepository.save(subject);
    }

    public Optional<Subject> getSubjectById(Long id) {
        return subjectRepository.findById(id);
    }

    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    @Transactional
    public Subject updateSubject(Long id, String name, String description) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with ID: " + id));

        if (!subject.getName().equals(name) && subjectRepository.existsByName(name)) {
            throw new IllegalArgumentException("Subject with name '" + name + "' already exists");
        }

        subject.setName(name);
        subject.setDescription(description);

        return subjectRepository.save(subject);
    }

    @Transactional
    public void deleteSubject(Long id) {
        if (!subjectRepository.existsById(id)) {
            throw new IllegalArgumentException("Subject not found with ID: " + id);
        }

        subjectRepository.deleteById(id);
    }
}
