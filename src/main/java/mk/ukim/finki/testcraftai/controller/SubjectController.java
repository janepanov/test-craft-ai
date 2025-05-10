package mk.ukim.finki.testcraftai.controller;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.testcraftai.model.Subject;
import mk.ukim.finki.testcraftai.service.SubjectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/teacher/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @GetMapping
    public String getSubjectsPage(Model model) {
        List<Subject> subjects = subjectService.getAllSubjects();
        model.addAttribute("subjects", subjects);
        return "teacher/subjects";
    }

    @GetMapping("/create")
    public String getCreateSubjectPage() {
        return "teacher/create-subject";
    }

    @PostMapping("/create")
    public String createSubject(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            RedirectAttributes redirectAttributes) {

        try {
            Subject subject = subjectService.createSubject(name, description);
            redirectAttributes.addFlashAttribute("success",
                    "Subject '" + subject.getName() + "' created successfully!");
            return "redirect:/teacher/subjects";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/teacher/subjects/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String getEditSubjectPage(@PathVariable Long id, Model model) {
        Subject subject = subjectService.getSubjectById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with ID: " + id));

        model.addAttribute("subject", subject);
        return "teacher/edit-subject";
    }

    @PostMapping("/edit/{id}")
    public String updateSubject(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            RedirectAttributes redirectAttributes) {

        try {
            Subject subject = subjectService.updateSubject(id, name, description);
            redirectAttributes.addFlashAttribute("success",
                    "Subject '" + subject.getName() + "' updated successfully!");
            return "redirect:/teacher/subjects";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/teacher/subjects/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteSubject(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            subjectService.deleteSubject(id);
            redirectAttributes.addFlashAttribute("success", "Subject deleted successfully!");
            return "redirect:/teacher/subjects";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/teacher/subjects";
        }
    }
}
