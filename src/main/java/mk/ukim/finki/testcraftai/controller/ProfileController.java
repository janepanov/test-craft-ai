package mk.ukim.finki.testcraftai.controller;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.testcraftai.model.User;
import mk.ukim.finki.testcraftai.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping("/profile")
    public String getProfilePage(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@AuthenticationPrincipal User currentUser,
                                @RequestParam String username,
                                @RequestParam String email,
                                RedirectAttributes redirectAttributes) {
        try {
            if (!currentUser.getUsername().equals(username) && userService.existsByUsername(username)) {
                throw new IllegalArgumentException("Username already exists");
            }

            if (!currentUser.getEmail().equals(email) && userService.existsByEmail(email)) {
                throw new IllegalArgumentException("Email already exists");
            }

            currentUser.setUsername(username);
            currentUser.setEmail(email);
            userService.save(currentUser);

            redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/profile";
    }
}
