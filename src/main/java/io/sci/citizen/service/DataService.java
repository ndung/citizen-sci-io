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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class DataService extends BaseService{

    private final DataRepository dataRepo;

    private final ProjectRepository projectRepo;

    public DataService(DataRepository dataRepo, ProjectRepository projectRepo) {
        this.dataRepo = dataRepo;
        this.projectRepo = projectRepo;
    }

    @Transactional(readOnly = true)
    public List<Data> findAll(Long projectId) {
        if (projectId==null){
            if (isAdmin()){
                return dataRepo.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
            }else{
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        }else{
            if (!isAdmin()){
                User user = getUser();
                Optional<Project> opt = projectRepo.findById(projectId);
                if (opt.isPresent() && !Objects.equals(opt.get().getCreator().getId(), user.getId())){
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
                }
            }
            return dataRepo.findByProject_IdOrderByCreatedAtDesc(projectId);
        }
    }

    public void updateStatus(Long id, int status){
        Optional<Data> opt = dataRepo.findById(id);
        if (opt.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Data data = opt.get();
        data.setStatus(status);
        dataRepo.save(data);
    }
}
