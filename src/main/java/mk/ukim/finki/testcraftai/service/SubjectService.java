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

    /**
     * Creates a new subject
     *
     * @param name the subject name
     * @param description the subject description
     * @return the created subject
     */
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

    /**
     * Retrieves a subject by ID
     *
     * @param id the subject ID
     * @return optional containing the subject if found
     */
    public Optional<Subject> getSubjectById(Long id) {
        return subjectRepository.findById(id);
    }

    /**
     * Retrieves all subjects
     *
     * @return list of all subjects
     */
    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    /**
     * Updates a subject
     *
     * @param id the subject ID
     * @param name the new name
     * @param description the new description
     * @return the updated subject
     */
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

    /**
     * Deletes a subject
     *
     * @param id the subject ID
     */
    @Transactional
    public void deleteSubject(Long id) {
        if (!subjectRepository.existsById(id)) {
            throw new IllegalArgumentException("Subject not found with ID: " + id);
        }

        subjectRepository.deleteById(id);
    }
}
