package io.sci.citizen.web;

import io.sci.citizen.model.User;
import io.sci.citizen.model.dto.ChangePasswordRequest;
import io.sci.citizen.model.dto.UserRequest;
import io.sci.citizen.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService service;

    public ProfileController(UserService service) {
        this.service = service;
    }

    @ModelAttribute("pwd")
    public ChangePasswordRequest pwdModel() {
        return new ChangePasswordRequest();
    }

    @ModelAttribute("form")
    public UserRequest formModel() {
        User user = service.getUser();
        UserRequest form = new UserRequest();
        form.setId(user.getId());
        form.setUsername(user.getUsername());
        form.setEmail(user.getEmail());
        form.setFullName(user.getFullName());
        return form;
    }

    @GetMapping
    public String view(Model model) {
        return "profile";
    }

    @PostMapping
    public String saveProfile(
            @Valid @ModelAttribute("form") UserRequest form,
            BindingResult br,
            RedirectAttributes ra) {

        User user = service.getUser();
        if (!user.getUsername().equals(form.getUsername()) &&
                service.isUserNameExisted(form.getUsername())) {
            br.rejectValue("username", "username.already.exists","Username already exists");
        }
        if (!user.getEmail().equals(form.getEmail()) &&
                service.isEmailExisted(form.getEmail())) {
            br.rejectValue("email", "email.already.exists","Email already exists");
        }
        if (br.hasErrors()) {
            ra.addFlashAttribute("form", form);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.form", br);
            return "redirect:/profile";
        }
        service.updateProfile(form);
        ra.addFlashAttribute("profileSaved", true);
        return "redirect:/profile";
    }

    @PostMapping("/password")
    public String changePassword(
            @Valid @ModelAttribute("pwd") ChangePasswordRequest pwd,
            BindingResult br,
            RedirectAttributes ra) {

        if (!br.hasErrors() && !pwd.getNewPassword().equals(pwd.getConfirmNewPassword())) {
            br.rejectValue("confirmNewPassword", "Match", "New passwords do not match");
        }
        if (br.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.pwd", br);
            ra.addFlashAttribute("pwd", pwd);
            return "redirect:/profile";
        }

        try {
            service.changePassword(pwd.getCurrentPassword(), pwd.getNewPassword());
            ra.addFlashAttribute("passwordChanged", true);
        } catch (IllegalArgumentException ex) {
            br.rejectValue("currentPassword", "Invalid", ex.getMessage());
            ra.addFlashAttribute("org.springframework.validation.BindingResult.pwd", br);
            ra.addFlashAttribute("pwd", pwd);
        }
        return "redirect:/profile";
    }
}
