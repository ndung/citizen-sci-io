package io.sci.citizen.web;

import io.sci.citizen.service.DataService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/data")
public class DataController {

    private final DataService service;

    public DataController(DataService service) {
        this.service = service;
    }

    @GetMapping
    public String list(@RequestParam(name = "projectId", required = false) Long projectId,
                       Model model) {
        model.addAttribute("data", service.findAll(projectId));
        return "data";
    }


}
