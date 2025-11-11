package io.sci.citizen.web;

import io.sci.citizen.config.FileDownload;
import io.sci.citizen.config.FileStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    private FileController controller;

    @Mock
    private FileStorage storage;

    @BeforeEach
    void setUp() {
        controller = new FileController(storage);
    }

    @Test
    void getReturnsRedirectWhenPublicUrlPresent() throws IOException, URISyntaxException {
        String key = "/public-key";
        URI publicUri = new URI("https://example.com/file");
        when(storage.publicUrl("public-key")).thenReturn(Optional.of(publicUri));

        ResponseEntity<?> response = controller.get(key);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(response.getHeaders().getLocation()).isEqualTo(publicUri);
        verify(storage, never()).download(key);
    }

    @Test
    void getStreamsFileWhenPublicUrlMissing() throws IOException, URISyntaxException {
        String key = "/stored-key";
        byte[] content = "hello".getBytes(StandardCharsets.UTF_8);
        FileDownload download = new FileDownload(new ByteArrayInputStream(content), content.length, "text/plain", "file.txt");
        when(storage.publicUrl("stored-key")).thenReturn(Optional.empty());
        when(storage.download(key)).thenReturn(Optional.of(download));

        ResponseEntity<?> response = controller.get(key);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)).isEqualTo("text/plain");
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .isEqualTo("inline; filename=\"file.txt\"");
        assertThat(response.getBody()).isInstanceOf(InputStreamResource.class);

        InputStreamResource resource = (InputStreamResource) response.getBody();
        assertThat(resource).isNotNull();
        assertThat(resource.getInputStream().readAllBytes()).isEqualTo(content);
    }

    @Test
    void getReturnsNotFoundWhenFileMissing() throws IOException, URISyntaxException {
        String key = "/missing";
        when(storage.publicUrl("missing")).thenReturn(Optional.empty());
        when(storage.download(key)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.get(key);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
