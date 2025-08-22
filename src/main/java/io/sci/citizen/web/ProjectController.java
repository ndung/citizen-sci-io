package io.sci.citizen.web;

import io.sci.citizen.config.FileStorage;
import io.sci.citizen.model.Project;
import io.sci.citizen.model.dto.ProjectRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.UUID;

@Controller
@RequestMapping("/projects")
public class ProjectController extends BaseController{

    @Autowired
    private FileStorage storage;

    @PostMapping
    public String create(@Valid @ModelAttribute("project") ProjectRequest form,
                         BindingResult binding,
                         @RequestParam(name = "iconFile", required = false) MultipartFile iconFile,
                         RedirectAttributes ra,
                         Model model) throws IOException, URISyntaxException {
        try {
            if (binding.hasErrors()) {
                model.addAttribute("projects", projectService.findAll());
                return "projects";
            }
            // upload icon if present
            if (iconFile != null && !iconFile.isEmpty()) {
                String storedName = storeIcon(iconFile); // <-- upload
                // Put stored filename into DTO so service persists it:
                form.setIcon(storedName);                // <-- adjust setter if your DTO differs
            }
            Project saved = projectService.create(form);
            ra.addFlashAttribute("projectSaved", true);
        }catch (Exception ex) {
            ex.printStackTrace();
            binding.rejectValue("icon", "icon.invalid", ex.getMessage());
            model.addAttribute("projects", projectService.findAll());
            return "projects";
        }
        return "redirect:/projects";
    }

    @GetMapping
    public String list(@RequestParam(name = "projectId", required = false) Long projectId,
                       Model model) {
        ProjectRequest form = new ProjectRequest();
        if(projectId!=null) {
            var entity = projectService.getById(projectId);
            form.setId(entity.getId());
            form.setIcon(entity.getIcon());
            form.setName(entity.getName());
            form.setEnabled(entity.isEnabled());
            form.setPubliclyAvailable(entity.isPubliclyAvailable());
            form.setDescription(entity.getDescription());
        }
        model.addAttribute("project", form);
        model.addAttribute("projects", projectService.findAll());
        return "projects";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("project") ProjectRequest form,
                         BindingResult binding,
                         @RequestParam(name = "iconFile", required = false) MultipartFile iconFile,
                         RedirectAttributes ra,
                         Model model) {
        try {
            if (!isAuthorized(id)){
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
            if (binding.hasErrors()) {
                model.addAttribute("projects", projectService.findAll());
            }
            if (iconFile != null && !iconFile.isEmpty()) {
                String storedName = storeIcon(iconFile);
                form.setIcon(storedName);
            }
            projectService.update(id, form);
            ra.addFlashAttribute("projectSaved", true);
        }catch (URISyntaxException ex) {
            binding.rejectValue("icon", "icon.invalid", ex.getMessage());
            model.addAttribute("projects", projectService.findAll());
            return "projects";
        } catch (IOException ex) {
            binding.rejectValue("icon", "icon.io", "Failed to store icon file.");
            model.addAttribute("projects", projectService.findAll());
            return "projects";
        }
        return "redirect:/projects";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id) {
        if (!isAuthorized(id)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return "redirect:/projects?projectId="+id;
    }

    @GetMapping("/{id}/config")
    public String configForm(@PathVariable("id") Long id) {
        if (!isAuthorized(id)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return "redirect:/sections?projectId="+id;
    }

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/png", "image/jpeg", "image/webp", "image/svg+xml"
    );


    private static String fileExtension(MultipartFile f) {
        String name = f.getOriginalFilename();
        if (!StringUtils.hasText(name)) return "";
        String ext = StringUtils.getFilenameExtension(name);
        return (ext == null) ? "" : ext;
    }

    private String storeIcon(MultipartFile iconFile) throws IOException, URISyntaxException {
        // Validate content type
        String contentType = iconFile.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Unsupported image type. Allowed: PNG, JPEG, WEBP, SVG.");
        }

        // Build a safe filename with extension
        String ext = fileExtension(iconFile);
        if (!StringUtils.hasText(ext)) {
            // last resort from content type
            ext = switch (contentType) {
                case "image/png" -> "png";
                case "image/jpeg" -> "jpg";
                case "image/webp" -> "webp";
                case "image/svg+xml" -> "svg";
                default -> "bin";
            };
        }
        String storedName = UUID.randomUUID().toString().replace("-", "") + "." + ext.toLowerCase();

        // Subfolder for icons:
        String relativeKey = "icon_" + storedName;
        return storage.store(relativeKey, iconFile).key();
    }
}
