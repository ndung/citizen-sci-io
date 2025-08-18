package io.sci.citizen.config;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

public interface FileStorage {
    StoredFile store(String key, MultipartFile file) throws IOException, URISyntaxException;
    Optional<FileDownload> download(String key) throws IOException; // for streaming if needed
    Optional<URI> publicUrl(String key) throws URISyntaxException; // may be empty if not public
}
