package io.sci.citizen.service;

import io.sci.citizen.model.Image;
import io.sci.citizen.model.User;
import io.sci.citizen.model.repository.ImageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.Optional;

@Service
public class RecordService extends BaseService {

    private final ImageRepository imageRepo;

    public RecordService(ImageRepository imageRepo) {
        this.imageRepo = imageRepo;
    }

    public Image getById(Long id) {
        Image image = imageRepo.findById(id).orElse(null);
        if (image == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        if (!isAdmin()) {
            User user = getUser();
            if (!Objects.equals(image.getData().getProject().getCreator().getId(), user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        }
        return image;
    }

    @Transactional
    public void updateStatus(Long id, int status) {
        Optional<Image> opt = imageRepo.findById(id);
        if (opt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        if (!isAdmin()) {
            User user = getUser();
            if (!Objects.equals(opt.get().getData().getProject().getCreator().getId(), user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        }
        Image image = opt.get();
        image.setStatus(status);
        imageRepo.save(image);
    }
}