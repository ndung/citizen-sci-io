package io.sci.citizen.web;

import io.sci.citizen.model.User;
import io.sci.citizen.model.dto.UserRequest;
import io.sci.citizen.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
public class UserAdminController {

    private final UserService service;

    public UserAdminController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public String list(@RequestParam(name = "userId", required = false) Long userId,
                       Model model) {
        UserRequest form = new UserRequest();
        if(userId!=null) {
            var entity = service.getById(userId);
            form.setId(entity.getId());
            form.setFullName(entity.getFullName());
            form.setEnabled(entity.isEnabled());
            form.setEmail(entity.getEmail());
            form.setUsername(entity.getUsername());
            form.setRolesCsv(String.join(", ", entity.getRoles()));
        }
        model.addAttribute("user", form);
        model.addAttribute("users", service.findAll());
        return "users";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("user") UserRequest form,
                         BindingResult binding,
                         RedirectAttributes ra,
                         Model model) {
        model.addAttribute("users", service.findAll());
        if (binding.hasErrors()) {
            return "users";
        }
        if (service.isUserNameExisted(form.getUsername())) {
            binding.rejectValue("username", "username.already.exists","Username already exists");
            return "users";
        }
        if (service.isEmailExisted(form.getEmail())) {
            binding.rejectValue("email", "email.already.exists","Email already exists");
            return "users";
        }
        if (form.getPassword() == null || form.getPassword().isBlank()) {
            binding.rejectValue("password", "password.is.required","Password is required");
            return "users";
        }
        if (!(form.getPassword().equals(form.getConfirmPassword()))){
            binding.rejectValue("confirmPassword","passwords.do.not.match","Passwords do not match");
            return "users";
        }
        service.create(form);
        ra.addFlashAttribute("userSaved", true);
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id) {
        return "redirect:/admin/users?userId="+id;
    }

    @PostMapping("/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("user") UserRequest form,
                         BindingResult binding,
                         RedirectAttributes ra,
                         Model model) {
        model.addAttribute("users", service.findAll());
        if (binding.hasErrors()) {
            return "users";
        }
        User user = service.getById(id);
        if (!user.getUsername().equals(form.getUsername()) &&
                service.isUserNameExisted(form.getUsername())) {
            binding.rejectValue("username", "username.already.exists","Username already exists");
            return "users";
        }
        if (!user.getEmail().equals(form.getEmail()) &&
                service.isEmailExisted(form.getEmail())) {
            binding.rejectValue("email", "email.already.exists","Email already exists");
            return "users";
        }
        if ((form.getPassword() != null && !form.getPassword().isBlank())||
                (form.getConfirmPassword() != null && !form.getConfirmPassword().isBlank())) {
            if (!(form.getPassword().equals(form.getConfirmPassword()))) {
                binding.rejectValue("confirmPassword", "passwords.do.not.match", "Passwords do not match");
                return "users";
            }
        }
        service.update(id, form);
        ra.addFlashAttribute("userSaved", true);
        return "redirect:/admin/users";
    }
}