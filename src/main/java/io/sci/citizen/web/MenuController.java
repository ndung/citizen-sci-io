package io.sci.citizen.web;

import io.sci.citizen.model.dto.MenuRequest;
import io.sci.citizen.service.MenuService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/menu")
public class MenuController {

    private final MenuService service;

    public MenuController(MenuService service) { this.service = service; }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", service.findAllFlatOrdered());
        return "menu/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("menuItem", new MenuRequest());
        model.addAttribute("parents", service.allParentsCandidates(null));
        return "menu/create";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("menuItem") MenuRequest form,
                         BindingResult binding,
                         RedirectAttributes ra,
                         Model model) {
        if (binding.hasErrors()) {
            model.addAttribute("parents", service.allParentsCandidates(null));
            return "menu/create";
        }
        service.create(form);
        ra.addFlashAttribute("success", "Menu item created.");
        return "redirect:/admin/menu";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        var entity = service.getById(id);
        model.addAttribute("menuId", id);
        model.addAttribute("menu", MenuRequest.fromEntity(entity));
        model.addAttribute("parents", service.allParentsCandidates(id));
        return "menu/edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("menu") MenuRequest form,
                         BindingResult binding,
                         RedirectAttributes ra,
                         Model model) {
        if (binding.hasErrors()) {
            model.addAttribute("menuId", id);
            model.addAttribute("parents", service.allParentsCandidates(id));
            return "menu/edit";
        }
        service.update(id, form);
        ra.addFlashAttribute("success", "Menu item updated.");
        return "redirect:/admin/menu";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra) {
        service.delete(id);
        ra.addFlashAttribute("success", "Menu item deleted.");
        return "redirect:/admin/menu";
    }
}