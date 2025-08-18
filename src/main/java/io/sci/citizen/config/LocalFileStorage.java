package io.sci.citizen.config;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class LocalFileStorage implements FileStorage {
    private final Path root;
    private final URI baseUrl;

    public LocalFileStorage(StorageProps props) {
        this.root = Paths.get(props.getLocal().getBasePath());
        this.baseUrl = URI.create(props.getLocal().getBaseUrl());
        try {
            Files.createDirectories(root);
        } catch (IOException ignored) {
        }
    }

    @Override
    public StoredFile store(String key, MultipartFile file) throws IOException {
        Path target = safeResolve(key);
        Files.createDirectories(target.getParent());
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        String ct = Optional.ofNullable(file.getContentType())
                .orElseGet(() -> probe(target));
        return new StoredFile(key, baseUrl.resolve(URLEncoder.encode(key, StandardCharsets.UTF_8)), file.getSize(), ct);
    }

    @Override
    public Optional<FileDownload> download(String key) throws IOException {
        Path p = safeResolve(key);
        if (!Files.exists(p)) return Optional.empty();
        String ct = probe(p);
        return Optional.of(new FileDownload(Files.newInputStream(p), Files.size(p), ct, Paths.get(key).getFileName().toString()));
    }

    @Override
    public Optional<URI> publicUrl(String key) {
        return Optional.of(baseUrl.resolve(URLEncoder.encode(key, StandardCharsets.UTF_8)));
    }

    private Path safeResolve(String key) {
        Path p = root.resolve(key).normalize();
        if (!p.startsWith(root)) throw new IllegalArgumentException("Invalid path");
        return p;
    }

    private String probe(Path p) {
        try {
            return Optional.ofNullable(Files.probeContentType(p)).orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        } catch (IOException e) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }
}