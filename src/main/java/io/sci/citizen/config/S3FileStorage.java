package io.sci.citizen.config;

import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Optional;

public class S3FileStorage implements FileStorage {
    private final S3Client s3;
    private final S3Presigner presigner;
    private final StorageProps props;

    public S3FileStorage(S3Client s3, S3Presigner presigner, StorageProps props) {
        this.s3 = s3; this.presigner = presigner; this.props = props;
    }

    @Override
    public StoredFile store(String key, MultipartFile file) throws IOException, URISyntaxException {
        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(props.getS3().getBucket())
                .key(key)
                .contentType(Optional.ofNullable(file.getContentType()).orElse("application/octet-stream"))
                .build();

        s3.putObject(put, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        URI getUrl = presignGet(key, Duration.ofMinutes(props.getS3().getUrlMinutes()));
        return new StoredFile(key, getUrl, file.getSize(), put.contentType());
    }

    @Override
    public Optional<FileDownload> download(String key) {
        // we prefer presigned URLs; streaming via app is optional:
        return Optional.empty();
    }

    @Override
    public Optional<URI> publicUrl(String key) throws URISyntaxException {
        return Optional.of(presignGet(key, Duration.ofMinutes(props.getS3().getUrlMinutes())));
    }

    private URI presignGet(String key, Duration ttl) throws URISyntaxException {
        GetObjectRequest get = GetObjectRequest.builder()
                .bucket(props.getS3().getBucket())
                .key(key)
                .build();
        GetObjectPresignRequest req = GetObjectPresignRequest.builder()
                .signatureDuration(ttl)
                .getObjectRequest(get)
                .build();
        return presigner.presignGetObject(req).url().toURI();
    }
}
