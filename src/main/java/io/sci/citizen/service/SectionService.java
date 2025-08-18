package io.sci.citizen.service;

import io.sci.citizen.model.Project;
import io.sci.citizen.model.Section;
import io.sci.citizen.model.User;
import io.sci.citizen.model.dto.SectionRequest;
import io.sci.citizen.model.repository.ProjectRepository;
import io.sci.citizen.model.repository.SectionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class SectionService extends BaseService{

    private final SectionRepository sectionRepo;
    private final ProjectRepository projectRepo;

    public SectionService(SectionRepository sectionRepo, ProjectRepository projectRepo) {
        this.sectionRepo = sectionRepo;
        this.projectRepo = projectRepo;
    }

    @Transactional(readOnly = true)
    public List<Section> list(Optional<Long> projectId) {
        if (projectId.isEmpty()){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (!isAdmin()){
            User user = getUser();
            Optional<Project> opt = projectRepo.findById(projectId.get());
            if (opt.isPresent() && !Objects.equals(opt.get().getCreator().getId(), user.getId())){
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        }
        return projectId.map(sectionRepo::findByProject_IdOrderBySequenceAsc)
                .orElseGet(sectionRepo::findAllByOrderBySequenceAsc);
    }

    @Transactional(readOnly = true)
    public Section getById(Long id) {
        return sectionRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Section create(SectionRequest req) {
        Section s = new Section();
        apply(s, req);
        return sectionRepo.save(s);
    }

    @Transactional
    public Section update(Long id, SectionRequest req) {
        Section s = getById(id);
        apply(s, req);
        return sectionRepo.save(s);
    }

    @Transactional
    public void delete(Long id) {
        sectionRepo.deleteById(id);
    }

    private void apply(Section s, SectionRequest req) {
        s.setSequence(req.getSequence());
        s.setType(req.getType().trim());
        s.setName(req.getName().trim());
        s.setEnabled(req.isEnabled());

        if (req.getProjectId() != null) {
            Project p = projectRepo.findById(req.getProjectId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project not found"));
            s.setProject(p);
        } else {
            s.setProject(null);
        }
    }
}