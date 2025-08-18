package io.sci.citizen.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@EnableConfigurationProperties(StorageProps.class)
public class S3StorageConfig {

    @Bean
    @ConditionalOnProperty(name = "app.storage.type", havingValue = "s3")
    public FileStorage s3Storage(StorageProps props) {
        Region region = Region.of(props.getS3().getRegion());

        AwsCredentialsProvider creds;
        if (props.getS3().getAccessKey() != null && !props.getS3().getAccessKey().isBlank()) {
            if (props.getS3().getSessionToken() != null && !props.getS3().getSessionToken().isBlank()) {
                creds = StaticCredentialsProvider.create(
                        AwsSessionCredentials.create(
                                props.getS3().getAccessKey(),
                                props.getS3().getSecretKey(),
                                props.getS3().getSessionToken()
                        )
                );
            } else {
                creds = StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                props.getS3().getAccessKey(),
                                props.getS3().getSecretKey()
                        )
                );
            }
        } else if (props.getS3().getProfile() != null) {
            creds = ProfileCredentialsProvider.create(props.getS3().getProfile());
        } else {
            creds = DefaultCredentialsProvider.create(); // fallback
        }
        S3Client s3 = S3Client.builder().region(region).credentialsProvider(creds).build();
        S3Presigner presigner = S3Presigner.builder().region(region).credentialsProvider(creds).build();
        return new S3FileStorage(s3, presigner, props);
    }
}