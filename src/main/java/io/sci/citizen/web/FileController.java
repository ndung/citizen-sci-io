package io.sci.citizen.web;

import io.sci.citizen.config.FileStorage;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/files")
public class FileController {
    private final FileStorage storage;
    public FileController(FileStorage storage) { this.storage = storage; }

    @GetMapping("/{*key}")
    public ResponseEntity<?> get(@PathVariable("key") String key) throws IOException, URISyntaxException {
        var url = storage.publicUrl(key.substring(1));
        System.out.println("url: " + url);
        if (url.isPresent()) {
            System.out.println("uri: " + url.get());
            return ResponseEntity.status(HttpStatus.FOUND).location(url.get()).build();
        }
        return storage.download(key)
                .map(d -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(d.contentType()))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + d.filename() + "\"")
                        .body(new InputStreamResource(d.stream())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}