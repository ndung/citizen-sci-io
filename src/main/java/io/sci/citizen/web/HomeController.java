package io.sci.citizen.web;

import io.sci.citizen.model.Project;
import io.sci.citizen.model.dto.ProjectData;
import io.sci.citizen.service.DataService;
import io.sci.citizen.service.ProjectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {

    private final ProjectService projectService;
    private final DataService dataService;

    public HomeController(ProjectService projectService, DataService dataService) {
        this.projectService = projectService;
        this.dataService = dataService;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Project> projectList = projectService.findAll();
        List<ProjectData> projectDataList = new ArrayList<>();
        for (Project project : projectList) {
            List<Integer> summary = dataService.getProjectSummary(project.getId());
            String contributors = ". Contributors: "+summary.get(0);
            if (summary.get(0) > 1) {
                contributors = contributors + " users";
            }else{
                contributors = contributors + " user";
            }
            String records = ", data: "+summary.get(1);
            if (summary.get(1) > 1) {
                records = records + " records.";
            }else{
                records = records + " record.";
            }
            String description = "Project was created at " + sdf.format(project.getCreatedAt()) + contributors + records;
            ProjectData projectData = new ProjectData(project, description);
            projectDataList.add(projectData);
        }
        model.addAttribute("projects", projectDataList);
        return "dashboard";
    }
}
