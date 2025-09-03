package io.sci.citizen.web;

import io.sci.citizen.service.DataService;
import io.sci.citizen.service.RecordService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/record")
public class RecordController {

    private final DataService dataService;
    private final RecordService  recordService;

    public RecordController(DataService dataService, RecordService recordService) {
        this.dataService = dataService;
        this.recordService = recordService;

    }

    @GetMapping("/{id}")
    public String list(@PathVariable("id") Long dataId,
                       Model model) {
        model.addAttribute("record", dataService.getById(dataId));
        return "records";
    }

    @PostMapping("/{recordId}/approve")
    public String approveData(@PathVariable("recordId") Long dataId) {
        dataService.updateStatus(dataId, 1);
        return "redirect:/record/"+dataId;
    }

    @PostMapping("/{recordId}/reject")
    public String rejectData(@PathVariable("recordId") Long dataId) {
        dataService.updateStatus(dataId, 2);
        return "redirect:/record/"+dataId;
    }

    @PostMapping("/{recordId}/{imageId}/approve")
    public String approveImage(@PathVariable("recordId") Long dataId,
                             @PathVariable("imageId") Long imageId) {
        recordService.updateStatus(imageId, 1);
        return "redirect:/record/"+dataId;
    }

    @PostMapping("/{recordId}/{imageId}/reject")
    public String rejectImage(@PathVariable("recordId") Long dataId,
                               @PathVariable("imageId") Long imageId) {
        recordService.updateStatus(imageId, 2);
        return "redirect:/record/"+dataId;
    }
}
