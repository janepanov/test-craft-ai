package mk.ukim.finki.testcraftai.controller;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.testcraftai.model.User;
import mk.ukim.finki.testcraftai.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        model.addAttribute("user", user);
        return "admin/dashboard";
    }

    @GetMapping("/teacher/dashboard")
    public String teacherDashboard(Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        model.addAttribute("user", user);
        return "teacher/dashboard";
    }

    @GetMapping("/student/dashboard")
    public String studentDashboard(Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        model.addAttribute("user", user);
        return "student/dashboard";
    }
}
