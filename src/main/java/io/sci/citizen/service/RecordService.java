package io.sci.citizen.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.sci.citizen.api.component.Diff;
import io.sci.citizen.api.dto.RecordData;
import io.sci.citizen.config.FileStorage;
import io.sci.citizen.model.*;
import io.sci.citizen.model.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

@Service
public class RecordService extends BaseService {

    private final DataRepository dataRepo;
    private final ImageRepository imageRepo;
    private final ProjectRepository projectRepo;
    private final SectionRepository sectionRepo;
    private final TextQueryRepository textQueryRepo;
    private final QueryReplyRepository queryReplyRepo;
    private final FileStorage fileStorage;

    public RecordService(DataRepository dataRepo, ImageRepository imageRepo,
                         ProjectRepository projectRepo, SectionRepository sectionRepo,
                         TextQueryRepository textQueryRepo, QueryReplyRepository queryReplyRepo,
                         FileStorage fileStorage) {
        this.dataRepo = dataRepo;
        this.imageRepo = imageRepo;
        this.projectRepo = projectRepo;
        this.sectionRepo = sectionRepo;
        this.textQueryRepo = textQueryRepo;
        this.queryReplyRepo = queryReplyRepo;
        this.fileStorage = fileStorage;
    }

    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").enableComplexMapKeySerialization().create();

    @Transactional
    public void record(Long userId, String record, MultipartFile[] images, String results) throws IOException, URISyntaxException {
        RecordData model = gson.fromJson(record, RecordData.class);
        List<Data> list = dataRepo.findByUser_IdAndUuidOrderByCreatedAtDesc(userId, model.uuid());
        Data data = new Data();
        if (list!=null && !list.isEmpty()){
            data = list.get(0);
            data.setUpdatedAt(new Date());
        }else{
            data.setCreatedAt(new Date());
        }
        data.setLatitude(model.latitude());
        data.setLongitude(model.longitude());
        data.setAccuracy(model.accuracy());
        Optional<Project> project = projectRepo.findById(model.projectId());
        project.ifPresent(data::setProject);
        data.setUuid(model.uuid());
        data.setStartDate(model.startDate());
        data.setFinishDate(model.finishDate());
        Optional<User> user = userRepo.findById(userId);
        user.ifPresent(data::setUser);
        data = dataRepo.save(data);
        if (images != null) {
            List<Image> currentList = imageRepo.findByDataId(data.getId());
            List<Image> onlyInFirst = new ArrayList<>();
            List<Image> onlyInSecond = new ArrayList<>();
            if (currentList!=null && !currentList.isEmpty()) {
                List<Image> newList = new ArrayList<>();
                for (MultipartFile image : images) {
                    String name = image.getOriginalFilename();
                    String sectionId = name.substring(0, name.indexOf("-"));
                    Image newImage = new Image();
                    newImage.setData(data);
                    Section section = new Section();
                    section.setId(Long.parseLong(sectionId));
                    newImage.setSection(section);
                    newImage.setOriginalFileName(name);
                    newList.add(newImage);
                }
                Diff.Result<Image> r = Diff.diffByKey(
                        newList, currentList,
                        i -> (i.getData().getUuid() + "|" + i.getSection().getId() + "|" + i.getOriginalFileName()).toLowerCase(Locale.ROOT)
                );
                onlyInFirst = r.onlyInFirst;
                onlyInSecond = r.onlyInSecond;
            }
            for (MultipartFile image : images){
                String name = image.getOriginalFilename();
                boolean isNew = false;
                if (!onlyInFirst.isEmpty()){
                    isNew = onlyInFirst.stream().anyMatch(i -> name.equalsIgnoreCase(i.getOriginalFileName()));
                }else{
                    isNew = true;
                }
                if (isNew) {
                    String ext = Optional.ofNullable(name)
                            .filter(n -> n.contains("."))
                            .map(n -> n.substring(n.lastIndexOf('.')))
                            .orElse("");
                    String sectionId = name.substring(0, name.indexOf("-"));
                    String key = data.getProject().getId() + "_" + sectionId + "_" + data.getId() + "_" + UUID.randomUUID() + ext;
                    String path = fileStorage.store(key, image).key();
                    Optional<Section> section = sectionRepo.findById(Long.parseLong(sectionId));
                    Image recordImage = new Image();
                    recordImage.setUuid(path);
                    recordImage.setOriginalFileName(name);
                    recordImage.setData(data);
                    recordImage.setStatus(0);
                    section.ifPresent(recordImage::setSection);
                    imageRepo.save(recordImage);
                }
            }
            for (Image image : onlyInSecond){
                imageRepo.deleteObsoleteImage(data.getId(), image.getSection().getId(), image.getOriginalFileName());
            }
        }
        if (results!=null) {
            Map<String,Object> map = gson.fromJson(results, Map.class);
            for (String key : map.keySet()) {
                Optional<TextQuery> question = textQueryRepo.findById(Long.parseLong(key));
                if (question.isPresent()) {
                    QueryReply response = queryReplyRepo.findByData_idAndQuestion_Id(data.getId(), question.get().getId());
                    if (response == null) {
                        response = new QueryReply();
                        response.setData(data);
                        response.setQuestion(question.get());
                    }
                    Object value = map.get(key);
                    if (value instanceof List) {
                        response.setResponse(gson.toJson(value));
                    } else {
                        response.setResponse((String) value);
                    }
                    response.setResponseDateTime(new Date());
                    queryReplyRepo.save(response);
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public int[] getRecordsSummaryByUser(Long userId){
        int uploaded = dataRepo.getRecordCountByUserId(userId);
        int verified = dataRepo.getRecordCountByUserIdAndStatus(userId, 1);
        int total = dataRepo.getRecordCount();
        return new int[]{uploaded, verified, total};
    }

    @Transactional(readOnly = true)
    public List<Data> getRecordsByUser(Long userId, Integer type){
        if (type==2){
            return dataRepo.findAll();
        }else if (type==1){
            return dataRepo.findByUser_IdAndStatusOrderByCreatedAtDesc(userId,1);
        }else {
            return dataRepo.findByUser_IdOrderByCreatedAtDesc(userId);
        }
    }

    @Transactional(readOnly = true)
    public int[] getRecordsSummaryByUserAndProject(Long userId, Long projectId){
        int uploaded = dataRepo.getRecordCountByProjectIdAndUserId(projectId, userId);
        int verified = dataRepo.getRecordCountByProjectIdAndUserIdAndStatus(projectId, userId, 1);
        int total = dataRepo.getRecordCountByProjectId(projectId);
        return new int[]{uploaded, verified, total};
    }

    @Transactional(readOnly = true)
    public List<Data> getRecordsByUserAndProject(Long userId, Long projectId, Integer type){
        if (type==2){
            return dataRepo.findByProject_IdOrderByCreatedAtDesc(projectId);
        }else if (type==1) {
            return dataRepo.findByProject_IdAndUser_IdAndStatusOrderByCreatedAtDesc(projectId, userId,1);
        }else {
            return dataRepo.findByProject_IdAndUser_IdOrderByCreatedAtDesc(projectId, userId);
        }
    }

    @Transactional(readOnly = true)
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