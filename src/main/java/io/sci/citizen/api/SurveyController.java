package io.sci.citizen.api;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.*;

import io.sci.citizen.model.Data;
import io.sci.citizen.model.QueryReply;
import io.sci.citizen.model.TextQuery;
import io.sci.citizen.model.repository.DataRepository;
import io.sci.citizen.model.repository.QueryReplyRepository;
import io.sci.citizen.model.repository.TextQueryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/survey")
public class SurveyController extends BaseApiController {

    @Autowired
    private TextQueryRepository surveyQuestionRepository;

    @Autowired
    private QueryReplyRepository surveyResponseRepository;

    @Autowired
    private DataRepository dataRepository;

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public ResponseEntity<Response> upload(@RequestHeader("Authorization")  String token,
                                           @RequestParam("dataId") String dataId,
                                           @RequestParam("results") String results){
        try {
            if (!authorize(token)) {
                return FORBIDDEN;
            }
            String userId = getUserId(token);
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").enableComplexMapKeySerialization().create();
            Map<String,Object> map = gson.fromJson(results, Map.class);
            Data data = dataRepository.findDataByUser_IdAndUuidOrderByCreatedAtDesc(Long.parseLong(userId), dataId);
            for (String key : map.keySet()){

                Optional<TextQuery> question = surveyQuestionRepository.findById(Long.parseLong(key));
                if  (question.isPresent()) {
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
            return getHttpStatus(new Response(Boolean.TRUE));
        } catch (Exception e) {
            e.printStackTrace();
            return getHttpStatus(new Response(e.getMessage()));
        }
    }


}
