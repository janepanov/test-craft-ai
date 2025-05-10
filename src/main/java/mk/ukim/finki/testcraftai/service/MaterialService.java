package mk.ukim.finki.testcraftai.service;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.testcraftai.model.Material;
import mk.ukim.finki.testcraftai.model.Subject;
import mk.ukim.finki.testcraftai.model.User;
import mk.ukim.finki.testcraftai.repository.MaterialRepository;
import mk.ukim.finki.testcraftai.repository.SubjectRepository;
import mk.ukim.finki.testcraftai.repository.UserRepository;
import mk.ukim.finki.testcraftai.util.FileProcessingUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;

    /**
     * Uploads and processes a new material
     *
     * @param file the uploaded file
     * @param title the title of the material
     * @param subjectId the subject ID
     * @param username the username of the uploader
     * @return the saved material
     * @throws IOException if file processing fails
     */
    @Transactional
    public Material uploadMaterial(MultipartFile file, String title, Long subjectId, String username) throws IOException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with ID: " + subjectId));

        // Extract text content from the file
        String content = FileProcessingUtil.extractTextFromFile(file);

        // Determine file type
        Material.FileType fileType = FileProcessingUtil.determineFileType(file.getOriginalFilename());

        // Create and save the material
        Material material = new Material();
        material.setTitle(title);
        material.setContent(content);
        material.setFileType(fileType);
        material.setSubject(subject);
        material.setUser(user);

        return materialRepository.save(material);
    }

    /**
     * Retrieves a material by ID
     *
     * @param id the material ID
     * @return optional containing the material if found
     */
    public Optional<Material> getMaterialById(Long id) {
        return materialRepository.findById(id);
    }

    /**
     * Retrieves all materials for a user
     *
     * @param username the username
     * @param pageable pagination information
     * @return page of materials
     */
    public Page<Material> getMaterialsByUser(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return materialRepository.findByUser(user, pageable);
    }

    /**
     * Retrieves all materials for a subject
     *
     * @param subjectId the subject ID
     * @param pageable pagination information
     * @return page of materials
     */
    public Page<Material> getMaterialsBySubject(Long subjectId, Pageable pageable) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with ID: " + subjectId));

        return materialRepository.findBySubject(subject, pageable);
    }

    /**
     * Retrieves all materials
     *
     * @return list of all materials
     */
    public List<Material> getAllMaterials() {
        return materialRepository.findAll();
    }
}
