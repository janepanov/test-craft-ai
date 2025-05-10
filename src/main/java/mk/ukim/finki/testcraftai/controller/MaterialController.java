package mk.ukim.finki.testcraftai.controller;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.testcraftai.model.Material;
import mk.ukim.finki.testcraftai.model.Subject;
import mk.ukim.finki.testcraftai.service.MaterialService;
import mk.ukim.finki.testcraftai.service.SubjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/teacher/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;
    private final SubjectService subjectService;

    @GetMapping
    public String getMaterialsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication,
            Model model) {

        Page<Material> materials = materialService.getMaterialsByUser(
                authentication.getName(),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        model.addAttribute("materials", materials);
        return "teacher/materials";
    }

    @GetMapping("/upload")
    public String getUploadMaterialPage(Model model) {
        List<Subject> subjects = subjectService.getAllSubjects();
        model.addAttribute("subjects", subjects);
        return "teacher/upload-material";
    }

    @PostMapping("/upload")
    public String uploadMaterial(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("subjectId") Long subjectId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            Material material = materialService.uploadMaterial(
                    file, title, subjectId, authentication.getName()
            );

            redirectAttributes.addFlashAttribute("success",
                    "Material '" + material.getTitle() + "' uploaded successfully!");
            return "redirect:/teacher/materials";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error",
                    "Failed to upload material: " + e.getMessage());
            return "redirect:/teacher/materials/upload";
        }
    }

    @GetMapping("/{id}")
    public String getMaterialDetails(@PathVariable Long id, Model model) {
        Material material = materialService.getMaterialById(id)
                .orElseThrow(() -> new IllegalArgumentException("Material not found with ID: " + id));

        model.addAttribute("material", material);
        return "teacher/material-details";
    }
}