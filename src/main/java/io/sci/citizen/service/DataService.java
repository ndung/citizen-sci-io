package io.sci.citizen.service;

import io.sci.citizen.model.Data;
import io.sci.citizen.model.Project;
import io.sci.citizen.model.User;
import io.sci.citizen.model.repository.DataRepository;
import io.sci.citizen.model.repository.ProjectRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class DataService extends BaseService {

    private final DataRepository dataRepo;

    private final ProjectRepository projectRepo;

    public DataService(DataRepository dataRepo, ProjectRepository projectRepo) {
        this.dataRepo = dataRepo;
        this.projectRepo = projectRepo;
    }

    public List<Integer> getProjectSummary(Long projectId){
        Integer user = dataRepo.getUserCountByProjectId(projectId);
        Integer record = dataRepo.getRecordCountByProjectId(projectId);
        return List.of(user, record);
    }

    @Transactional(readOnly = true)
    public List<Data> findAll() {
        if (!isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return dataRepo.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Transactional(readOnly = true)
    public List<Data> findAll(Long projectId) {
        if (!isAdmin()) {
            User user = getUser();
            Optional<Project> opt = projectRepo.findById(projectId);
            if (opt.isPresent() && !Objects.equals(opt.get().getCreator().getId(), user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        }
        return dataRepo.findByProject_IdOrderByCreatedAtDesc(projectId);
    }

    @Transactional(readOnly = true)
    public Data getById(Long id) {
        Data data = dataRepo.findById(id).orElse(null);
        if (data == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        if (!isAdmin()) {
            User user = getUser();
            if (!Objects.equals(data.getProject().getCreator().getId(), user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        }
        return data;
    }

    @Transactional
    public void updateStatus(Long id, int status) {
        Optional<Data> opt = dataRepo.findById(id);
        if (opt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        if (!isAdmin()) {
            User user = getUser();
            if (!Objects.equals(opt.get().getProject().getCreator().getId(), user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        }
        Data data = opt.get();
        data.setVerificator(getUser());
        data.setVerifiedAt(new Date());
        data.setStatus(status);
        dataRepo.save(data);
    }
}
