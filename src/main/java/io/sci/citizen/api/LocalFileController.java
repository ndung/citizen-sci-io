package io.sci.citizen.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/file")
public class LocalFileController {

    @Value("${app.storage.local.base-path}")
    private String location;

    @RequestMapping(value = "/{fileName:.+}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getImage(@PathVariable("fileName") String fileName) {
        HttpHeaders headers = new HttpHeaders();
        try {
            Path filePath = resolvePath(fileName);
            byte[] media = Files.readAllBytes(filePath);

            headers.setCacheControl(CacheControl.noCache().getHeaderValue());
            return new ResponseEntity<>(media, headers, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/images/{subfolder}/{fileName:.+}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getFile(@PathVariable("subfolder") String subfolder,
                                          @PathVariable("fileName") String fileName) {
        HttpHeaders headers = new HttpHeaders();

        try {
            Path filePath = resolvePath(subfolder, fileName);
            byte[] media = Files.readAllBytes(filePath);

            headers.setCacheControl(CacheControl.noCache().getHeaderValue());
            return new ResponseEntity<>(media, headers, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);
        }
    }

    private Path resolvePath(String... parts) {
        return Paths.get(location, parts);
    }
}
