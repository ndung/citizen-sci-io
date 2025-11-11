package io.sci.citizen.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3FileStorageTest {

    @Mock
    private S3Client s3;

    @Mock
    private S3Presigner presigner;

    private StorageProps props;

    private S3FileStorage storage;

    @BeforeEach
    void setUp() {
        props = new StorageProps();
        props.getS3().setBucket("test-bucket");
        props.getS3().setUrlMinutes(15);
        storage = new S3FileStorage(s3, presigner, props);
    }

    @Test
    void storeUploadsFileAndReturnsStoredFileWithPresignedUrl() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", "text/plain", "hello".getBytes());
        URI expectedUri = new URI("https://example.com/hello.txt");
        PresignedGetObjectRequest presigned = mock(PresignedGetObjectRequest.class);
        when(presigned.url()).thenReturn(expectedUri.toURL());
        when(presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presigned);

        StoredFile stored = storage.store("uploads/hello.txt", file);

        assertThat(stored.key()).isEqualTo("uploads/hello.txt");
        assertThat(stored.url()).isEqualTo(expectedUri);
        assertThat(stored.size()).isEqualTo(file.getSize());
        assertThat(stored.contentType()).isEqualTo("text/plain");

        ArgumentCaptor<PutObjectRequest> putCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3).putObject(putCaptor.capture(), any(RequestBody.class));
        PutObjectRequest put = putCaptor.getValue();
        assertThat(put.bucket()).isEqualTo("test-bucket");
        assertThat(put.key()).isEqualTo("uploads/hello.txt");
        assertThat(put.contentType()).isEqualTo("text/plain");

        ArgumentCaptor<GetObjectPresignRequest> presignCaptor = ArgumentCaptor.forClass(GetObjectPresignRequest.class);
        verify(presigner).presignGetObject(presignCaptor.capture());
        GetObjectPresignRequest presignRequest = presignCaptor.getValue();
        assertThat(presignRequest.signatureDuration()).isEqualTo(Duration.ofMinutes(15));
        assertThat(presignRequest.getObjectRequest().bucket()).isEqualTo("test-bucket");
        assertThat(presignRequest.getObjectRequest().key()).isEqualTo("uploads/hello.txt");
    }

    @Test
    void storeFallsBackToOctetStreamWhenContentTypeMissing() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "data.bin", null, new byte[] {1, 2, 3});
        URI expectedUri = new URI("https://example.com/data.bin");
        PresignedGetObjectRequest presigned = mock(PresignedGetObjectRequest.class);
        when(presigned.url()).thenReturn(expectedUri.toURL());
        when(presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presigned);

        storage.store("uploads/data.bin", file);

        ArgumentCaptor<PutObjectRequest> putCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3).putObject(putCaptor.capture(), any(RequestBody.class));
        PutObjectRequest put = putCaptor.getValue();
        assertThat(put.contentType()).isEqualTo("application/octet-stream");
    }

    @Test
    void publicUrlReturnsPresignedUri() throws Exception {
        URI expectedUri = new URI("https://example.com/fetch.txt");
        PresignedGetObjectRequest presigned = mock(PresignedGetObjectRequest.class);
        when(presigned.url()).thenReturn(expectedUri.toURL());
        when(presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presigned);

        assertThat(storage.publicUrl("uploads/fetch.txt")).contains(expectedUri);

        ArgumentCaptor<GetObjectPresignRequest> captor = ArgumentCaptor.forClass(GetObjectPresignRequest.class);
        verify(presigner).presignGetObject(captor.capture());
        GetObjectPresignRequest request = captor.getValue();
        assertThat(request.signatureDuration()).isEqualTo(Duration.ofMinutes(15));
        assertThat(request.getObjectRequest().bucket()).isEqualTo("test-bucket");
        assertThat(request.getObjectRequest().key()).isEqualTo("uploads/fetch.txt");
    }
}
