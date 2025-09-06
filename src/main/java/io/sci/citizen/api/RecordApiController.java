package io.sci.citizen.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.sci.citizen.api.dto.RecordData;
import io.sci.citizen.config.FileStorage;
import io.sci.citizen.model.*;
import io.sci.citizen.model.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/record")
public class RecordApiController extends BaseApiController {

    @Autowired
    private FileStorage fileStorage;
    @Autowired
    private DataRepository dataRepository;
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private SectionRepository sectionRepository;
    @Autowired
    private TextQueryRepository surveyQuestionRepository;
    @Autowired
    private QueryReplyRepository surveyResponseRepository;
    @Autowired
    private UserRepository userRepository;

    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").enableComplexMapKeySerialization().create();

    @Autowired
    private ProjectRepository projectRepository;

    @PostMapping(path = {"/upload", "/upload/"})
    public ResponseEntity<Response> upload(@RequestHeader("Authorization") String token,
                                           @RequestParam("model") String record,
                                           @RequestParam("images") MultipartFile[] images,
                                           @RequestParam("results") String results) {
        try {
            if (!authorize(token)) {
                return FORBIDDEN;
            }
            String userId = getUserId(token);
            RecordData model = gson.fromJson(record, RecordData.class);
            Data data = new Data();
            data.setCreatedAt(new Date());
            data.setLatitude(model.latitude());
            data.setLongitude(model.longitude());
            data.setAccuracy(model.accuracy());
            Optional<Project> project = projectRepository.findById(model.projectId());
            project.ifPresent(data::setProject);
            data.setUuid(model.uuid());
            data.setStartDate(model.startDate());
            data.setFinishDate(model.finishDate());
            Optional<User> user = userRepository.findById(Long.valueOf(userId));
            user.ifPresent(data::setUser);
            data = dataRepository.save(data);
            //imageRepository.deleteImagesByData_Id(data.getId());
            if (images != null) {
                for (MultipartFile image : images) {
                    String name = image.getOriginalFilename();
                    String ext = Optional.ofNullable(image.getOriginalFilename())
                            .filter(n -> n.contains("."))
                            .map(n -> n.substring(n.lastIndexOf('.')))
                            .orElse("");
                    String sectionId = name.substring(0,name.indexOf("-"));
                    String key = data.getProject().getId()+"_"+sectionId+"_"+data.getId()+"_"+UUID.randomUUID() + ext;
                    String path = fileStorage.store(key, image).key();
                    Optional<Section> section = sectionRepository.findById(Long.parseLong(sectionId));
                    Image recordImage = new Image();
                    recordImage.setUuid(path);
                    recordImage.setData(data);
                    recordImage.setStatus(0);
                    section.ifPresent(recordImage::setSection);
                    imageRepository.save(recordImage);
                }
            }
            Map<String,Object> map = gson.fromJson(results, Map.class);
            if (results!=null) {
                for (String key : map.keySet()) {
                    Optional<TextQuery> question = surveyQuestionRepository.findById(Long.parseLong(key));
                    if (question.isPresent()) {
                        QueryReply response = surveyResponseRepository.findByData_idAndQuestion_Id(data.getId(), question.get().getId());
                        if (response == null) {
                            response = new QueryReply();
                        }
                        response.setData(data);
                        response.setQuestion(question.get());
                        Object value = map.get(key);
                        if (value instanceof List) {
                            response.setResponse(gson.toJson(value));
                        } else {
                            response.setResponse((String) value);
                        }
                        response.setResponseDateTime(new Date());
                        surveyResponseRepository.save(response);
                    }
                }
            }
            return getHttpStatus(new Response(Boolean.TRUE));
        } catch (Exception e) {
            e.printStackTrace();
            return getHttpStatus(new Response(e.getMessage()));
        }
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ResponseEntity<Response> get(@RequestHeader("Authorization") String token) {
        try {
            if (!authorize(token)) {
                return FORBIDDEN;
            }
            return getHttpStatus(new Response(dataRepository.findAll()));

        } catch (Exception e) {
            e.printStackTrace();
            return getHttpStatus(new Response(e.getMessage()));
        }
    }

    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public ResponseEntity<Response> getList(@RequestHeader("Authorization") String token,
                                            @RequestBody String type) {
        try {
            if (!authorize(token)) {
                return FORBIDDEN;
            }
            String userId = getUserId(token);
            if (type.equals("2")){
                return getHttpStatus(new Response(dataRepository.findAll()));
            }else if (type.equals("0")) {
                return getHttpStatus(new Response(dataRepository.findDataByUser_IdAndStatusOrderByCreatedAtDesc(Long.parseLong(userId),1)));
            }else {
                return getHttpStatus(new Response(dataRepository.findDataByUser_IdOrderByCreatedAtDesc(Long.parseLong(userId))));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return getHttpStatus(new Response(e.getMessage()));
        }
    }

    @RequestMapping(value = "/summary", method = RequestMethod.GET)
    public ResponseEntity<Response> getSummary(@RequestHeader("Authorization") String token) {
        try {
            if (!authorize(token)) {
                return FORBIDDEN;
            }
            String userId = getUserId(token);
            List<Data> uploaded = dataRepository.findDataByUser_IdOrderByCreatedAtDesc(Long.parseLong(userId));
            List<Data> verified = dataRepository.findDataByUser_IdAndStatusOrderByCreatedAtDesc(Long.parseLong(userId), 1);
            List<Data> total = dataRepository.findAll();
            SummaryResponse summary = new SummaryResponse(verified==null ? 0: verified.size(),
                    uploaded==null ? 0: uploaded.size(), total.size());
            return getHttpStatus(new Response(summary));
        } catch (Exception e) {
            e.printStackTrace();
            return getHttpStatus(new Response(e.getMessage()));
        }
    }


    record SummaryResponse (int uploaded, int verified, int total){}
}
