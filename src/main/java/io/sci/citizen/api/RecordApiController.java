package io.sci.citizen.api;

import io.sci.citizen.api.dto.DataRequest;
import io.sci.citizen.api.dto.SummaryResponse;
import io.sci.citizen.model.*;
import io.sci.citizen.model.repository.*;
import io.sci.citizen.service.RecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/record")
public class RecordApiController extends BaseApiController {

    private final RecordService recordService;

    public RecordApiController(RecordService recordService) {
        this.recordService = recordService;
    }

    @PostMapping(path = {"/upload", "/upload/"})
    public ResponseEntity<Response> upload(@RequestHeader("Authorization") String token,
                                           @RequestParam("model") String model,
                                           @RequestParam(value = "images", required = false) MultipartFile[] images,
                                           @RequestParam(value = "results", required = false) String results) {
        try {
            if (!authorize(token)) {
                return FORBIDDEN;
            }
            recordService.record(Long.parseLong(getUserId(token)), model, images, results);
            return getHttpStatus(new Response(Boolean.TRUE));
        } catch (Exception e) {
            e.printStackTrace();
            return getHttpStatus(new Response(e.getMessage()));
        }
    }

    @RequestMapping(value = "/list-by-project", method = RequestMethod.POST)
    public ResponseEntity<Response> getListByProject(@RequestHeader("Authorization") String token,
                                                     @RequestBody DataRequest request) {
        try {
            if (!authorize(token)) {
                return FORBIDDEN;
            }
            String userId = getUserId(token);
            return getHttpStatus(new Response(recordService.getRecordsByUserAndProject(Long.parseLong(userId), request.projectId(), request.type())));
        } catch (Exception e) {
            e.printStackTrace();
            return getHttpStatus(new Response(e.getMessage()));
        }
    }

    @RequestMapping(value = "/list-by-user", method = RequestMethod.POST)
    public ResponseEntity<Response> getListByUser(@RequestHeader("Authorization") String token,
                                                  @RequestBody DataRequest request) {
        try {
            if (!authorize(token)) {
                return FORBIDDEN;
            }
            String userId = getUserId(token);
            return getHttpStatus(new Response(recordService.getRecordsByUser(Long.parseLong(userId), request.type())));
        } catch (Exception e) {
            e.printStackTrace();
            return getHttpStatus(new Response(e.getMessage()));
        }
    }

    @RequestMapping(value = "/project-summary", method = RequestMethod.GET)
    public ResponseEntity<Response> getProjectSummary(@RequestHeader("Authorization") String token,
                                                      @RequestBody DataRequest request) {
        try {
            if (!authorize(token)) {
                return FORBIDDEN;
            }
            String userId = getUserId(token);
            int[] arr = recordService.getRecordsSummaryByUserAndProject(Long.parseLong(userId), request.projectId());
            SummaryResponse summary = new SummaryResponse(arr[0], arr[1], arr[2]);
            return getHttpStatus(new Response(summary));
        } catch (Exception e) {
            e.printStackTrace();
            return getHttpStatus(new Response(e.getMessage()));
        }
    }

    @RequestMapping(value = "/user-summary", method = RequestMethod.GET)
    public ResponseEntity<Response> getUserSummary(@RequestHeader("Authorization") String token) {
        try {
            if (!authorize(token)) {
                return FORBIDDEN;
            }
            String userId = getUserId(token);
            int[] arr = recordService.getRecordsSummaryByUser(Long.parseLong(userId));
            SummaryResponse summary = new SummaryResponse(arr[0], arr[1], arr[2]);
            return getHttpStatus(new Response(summary));
        } catch (Exception e) {
            e.printStackTrace();
            return getHttpStatus(new Response(e.getMessage()));
        }
    }

}
