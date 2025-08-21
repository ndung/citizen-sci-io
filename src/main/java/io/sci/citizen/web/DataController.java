package io.sci.citizen.web;

import io.sci.citizen.service.DataService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/data")
public class DataController {

    private final DataService service;

    public DataController(DataService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public String list(@PathVariable("id") Long projectId,
                       Model model) {
        model.addAttribute("data", service.findAll(projectId));
        return "data";
    }


}
