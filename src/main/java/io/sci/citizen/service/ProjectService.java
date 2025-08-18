package io.sci.citizen.service;

import io.sci.citizen.model.Project;
import io.sci.citizen.model.User;
import io.sci.citizen.model.dto.ProjectRequest;
import io.sci.citizen.model.repository.ProjectRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProjectService extends BaseService{

    private final ProjectRepository projectRepo;

    public ProjectService(ProjectRepository projectRepo) {
        this.projectRepo = projectRepo;
    }

    @Transactional(readOnly = true)
    public List<Project> findAll() {
        if (isAdmin()){
            return projectRepo.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        }else{
            User user = getUser();
            if (user!=null){
                return projectRepo.findProjectsByCreator_IdOrderByCreatedAtDesc(user.getId());
            }
        }
        return null;
    }

    @Transactional(readOnly = true)
    public Map<Long,Project> findAll(Long userid) {
        List<Project> publicProjects = projectRepo.findProjectsByPubliclyAvailable(true);
        List<Project> privateProjects = projectRepo.findProjectsByCreator_IdOrderByCreatedAtDesc(userid);
        Map<Long,Project> projects = new HashMap<>();
        for (Project project : publicProjects){
            projects.put(project.getId(), project);
        }
        for (Project project : privateProjects){
            if (!projects.containsKey(project.getId())){
                projects.put(project.getId(), project);
            }
        }
        return projects;
    }

    @Transactional
    public Project create(ProjectRequest req) {
        Project project = req.toEntity();
        User user = getUser();
        if (user!=null){
            project.setCreator(user);
        }
        return projectRepo.save(project);
    }

    @Transactional(readOnly = true)
    public Project getById(Long id) {
        return projectRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Project update(Long id, ProjectRequest req) {
        Project p = getById(id);
        p.setName(req.getName());
        p.setEnabled(req.isEnabled());
        p.setIcon(req.getIcon());
        p.setDescription(req.getDescription());
        p.setPubliclyAvailable(req.isPubliclyAvailable());
        return projectRepo.save(p);
    }
}
