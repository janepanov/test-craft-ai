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

    @Transactional
    public Material uploadMaterial(MultipartFile file, String title, Long subjectId, String username) throws IOException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with ID: " + subjectId));

        String content = FileProcessingUtil.extractTextFromFile(file);

        Material.FileType fileType = FileProcessingUtil.determineFileType(file.getOriginalFilename());

        Material material = new Material();
        material.setTitle(title);
        material.setContent(content);
        material.setFileType(fileType);
        material.setSubject(subject);
        material.setUser(user);

        return materialRepository.save(material);
    }

    public Optional<Material> getMaterialById(Long id) {
        return materialRepository.findById(id);
    }

    public Page<Material> getMaterialsByUser(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return materialRepository.findByUser(user, pageable);
    }

    public Page<Material> getMaterialsBySubject(Long subjectId, Pageable pageable) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with ID: " + subjectId));

        return materialRepository.findBySubject(subject, pageable);
    }

    public List<Material> getAllMaterials() {
        return materialRepository.findAll();
    }
}
