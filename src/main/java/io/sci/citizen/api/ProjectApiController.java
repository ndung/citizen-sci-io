package io.sci.citizen.api;

import io.sci.citizen.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectApiController extends BaseApiController{

    private final ProjectService service;

    public ProjectApiController(ProjectService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<Response> get(@RequestHeader("Authorization") String token) {
        try {
            if (!authorize(token)) {
                return FORBIDDEN;
            }
            return getHttpStatus(new Response(service.findAll(Long.parseLong(getUserId(token)))));
        } catch (Exception e) {
            e.printStackTrace();
            return getHttpStatus(new Response(e.getMessage()));
        }
    }
}
