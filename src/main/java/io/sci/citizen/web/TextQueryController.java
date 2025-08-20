package io.sci.citizen.web;

import io.sci.citizen.model.QueryOption;
import io.sci.citizen.model.TextQuery;
import io.sci.citizen.model.dto.QueryOptionRequest;
import io.sci.citizen.model.dto.TextQueryRequest;
import io.sci.citizen.service.SectionService;
import io.sci.citizen.service.TextQueryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/questions")
public class TextQueryController {

    private final TextQueryService service;
    private final SectionService sectionService;

    public TextQueryController(TextQueryService svc, SectionService sectionSvc) {
        this.service = svc; this.sectionService = sectionSvc;
    }

    private Map<Integer,String> typeOptions() {
        return new LinkedHashMap<>() {{
            //put(1, "Bar");
            put(2, "Options");
            put(3, "String");
            put(4, "Multiple options");
            put(5, "Date");
			put(6, "Free input options");
			put(7, "Integer");
			put(8, "Decimal");
        }};
    }

    private void populate(Model model, Long sectionId, TextQueryRequest form){
        model.addAttribute("queries", service.list(sectionId));
        model.addAttribute("query", form);
        model.addAttribute("sections", List.of(sectionService.getById(sectionId)));
        model.addAttribute("typeOptions", typeOptions());
    }

    @GetMapping
    public String list(@RequestParam(name = "queryId", required = false) Long queryId,
                       @RequestParam(name = "sectionId", required = false) Long sectionId,
                       Model model) {
        TextQueryRequest form = new TextQueryRequest();
        form.getOptions().add(new QueryOptionRequest());
        if(queryId!=null) {
            var entity = service.getById(queryId);
            form.setId(entity.getId());
            form.setSequence(entity.getSequence());
            form.setType(entity.getType());
            form.setQuestion(entity.getQuestion());
            form.setEnabled(entity.isEnabled());
            form.setAttribute(entity.getAttribute());
            form.setRequired(entity.isRequired());
            form.setSectionId(entity.getSection().getId());
            List<QueryOptionRequest> list = new ArrayList<>();
            for (QueryOption option : entity.getOptions()){
                QueryOptionRequest request = new QueryOptionRequest();
                request.setId(option.getId());
                request.setEnabled(option.isEnabled());
                request.setSequence(option.getSequence());
                request.setDescription(option.getDescription());
                list.add(request);
            }
            form.setOptions(list);
        }
        populate(model, sectionId, form);
        return "questions";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("query") TextQueryRequest form,
                         BindingResult binding,
                         RedirectAttributes ra,
                         Model model) {
        if (binding.hasErrors()) {
            populate(model, form.getSectionId(), form);
            return "questions";
        }
        service.save(form);
        ra.addFlashAttribute("querySaved",true);
        return "redirect:/questions?sectionId="+form.getSectionId();
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id) {
        var entity = service.getById(id);
        return "redirect:/questions?sectionId="+entity.getSection().getId()+"&queryId="+id;
    }

    @PostMapping("/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("section") TextQueryRequest form,
                         BindingResult binding,
                         RedirectAttributes ra,
                         Model model) {
        if (binding.hasErrors()) {
            populate(model, form.getSectionId(), form);
            return "questions";
        }
        service.save(form);
        ra.addFlashAttribute("querySaved",true);
        return "redirect:/questions?sectionId="+form.getSectionId();
    }

}