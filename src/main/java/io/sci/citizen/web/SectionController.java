package io.sci.citizen.web;

import io.sci.citizen.model.Section;
import io.sci.citizen.model.dto.SectionRequest;
import io.sci.citizen.service.SectionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/sections")
public class SectionController extends BaseController{

    @Autowired
    private SectionService service;

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
        if (projectId!=null && !isAuthorized(projectId)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
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
        if (form.getProjectId()!=null && !isAuthorized(form.getProjectId())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (binding.hasErrors()) {
            populate(model, form.getProjectId(), form);
            return "sections";
        }
        service.create(form);
        ra.addFlashAttribute("sectionSaved",true);
        return "redirect:/sections?projectId="+form.getProjectId();
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id) {
        var entity = service.getById(id);
        if (!isAuthorized(entity.getProject())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return "redirect:/sections?projectId="+entity.getProject().getId()+"&sectionId="+id;
    }

    @PostMapping("/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("section") SectionRequest form,
                         BindingResult binding,
                         RedirectAttributes ra,
                         Model model) {
        if (form.getProjectId()!=null && !isAuthorized(form.getProjectId())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
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
        if (!isAuthorized(section.getProject())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        service.delete(id);
        ra.addFlashAttribute("success", "Section deleted.");
        return "redirect:/sections?projectId="+section.getProject().getId();
    }

    @GetMapping("/{id}/config")
    public String configForm(@PathVariable("id") Long id) {
        var entity = service.getById(id);
        if (!isAuthorized(entity.getProject())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return "redirect:/questions?sectionId="+id;
    }
}
