package io.sci.citizen.web;


import io.sci.citizen.model.Section;
import io.sci.citizen.model.dto.SectionRequest;
import io.sci.citizen.service.ProjectService;
import io.sci.citizen.service.SectionService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/sections")
public class SectionController {

    private final SectionService service;
    private final ProjectService projectService;

    public SectionController(SectionService service, ProjectService projectService) {
        this.service = service;
        this.projectService = projectService;
    }

    private Map<String,String> typeOptions() {
        return new java.util.LinkedHashMap<>() {{
            put("image", "IMG");
            put("survey", "TXT");
            put("location", "LOC");
        }};
    }

    private void populate(Model model, Long projectId, SectionRequest form){
        model.addAttribute("sections", service.list(Optional.ofNullable(projectId)));
        model.addAttribute("section", form);
        model.addAttribute("projects", List.of(projectService.getById(projectId)));
        model.addAttribute("typeOptions", typeOptions());
    }

    @GetMapping
    public String list(@RequestParam(name = "projectId", required = false) Long projectId,
                       @RequestParam(name = "sectionId", required = false) Long sectionId,
                       Model model) {
        SectionRequest form = new SectionRequest();
        if(sectionId!=null) {
            var entity = service.getById(sectionId);
            form.setId(entity.getId());
            form.setSequence(entity.getSequence());
            form.setType(entity.getType());
            form.setName(entity.getName());
            form.setEnabled(entity.isEnabled());
            form.setProjectId(entity.getProject() != null ? entity.getProject().getId() : null);
        }
        populate(model, projectId, form);
        return "sections";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("section") SectionRequest form,
                         BindingResult binding,
                         RedirectAttributes ra,
                         Model model) {
        if (binding.hasErrors()) {
            populate(model, form.getProjectId(), form);
            return "sections";
        }
        service.create(form);
        ra.addFlashAttribute("sectionSaved",true);
        return "redirect:/sections?projectId="+form.getProjectId();
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        var entity = service.getById(id);
        return "redirect:/sections?projectId="+entity.getProject().getId()+"&sectionId="+id;
    }

    @PostMapping("/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("section") SectionRequest form,
                         BindingResult binding,
                         RedirectAttributes ra,
                         Model model) {
        if (binding.hasErrors()) {
            populate(model, form.getProjectId(), form);
            return "sections";
        }
        service.update(id, form);
        ra.addFlashAttribute("sectionSaved",true);
        return "redirect:/sections?projectId="+form.getProjectId();
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra) {
        Section section = service.getById(id);
        service.delete(id);
        ra.addFlashAttribute("success", "Section deleted.");
        return "redirect:/sections?projectId="+section.getProject().getId();
    }

    @GetMapping("/{id}/config")
    public String configForm(@PathVariable("id") Long id) {
        return "redirect:/questions?sectionId="+id;
    }
}
