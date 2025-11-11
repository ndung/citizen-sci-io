package io.sci.citizen.config;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

class S3StorageConfigTest {

    @Test
    void createsStorageUsingBasicCredentialsWhenAccessKeyAndSecretProvided() {
        StorageProps props = storageProps();
        props.getS3().setAccessKey("access");
        props.getS3().setSecretKey("secret");

        TestBuilders builders = mockBuilders();

        try (MockedStatic<S3Client> s3ClientStatic = mockStatic(S3Client.class);
             MockedStatic<S3Presigner> presignerStatic = mockStatic(S3Presigner.class)) {
            s3ClientStatic.when(S3Client::builder).thenReturn(builders.clientBuilder);
            presignerStatic.when(S3Presigner::builder).thenReturn(builders.presignerBuilder);

            FileStorage storage = new S3StorageConfig().s3Storage(props);

            assertThat(storage).isInstanceOf(S3FileStorage.class);

            AwsCredentialsProvider provider = captureCredentialsProvider(builders.clientBuilder);
            assertThat(provider).isInstanceOf(StaticCredentialsProvider.class);
            AwsCredentials credentials = provider.resolveCredentials();
            assertThat(credentials).isInstanceOf(AwsBasicCredentials.class);
            AwsBasicCredentials basic = (AwsBasicCredentials) credentials;
            assertThat(basic.accessKeyId()).isEqualTo("access");
            assertThat(basic.secretAccessKey()).isEqualTo("secret");

            verify(builders.presignerBuilder).credentialsProvider(provider);
        }
    }

    @Test
    void createsStorageUsingSessionCredentialsWhenTokenProvided() {
        StorageProps props = storageProps();
        props.getS3().setAccessKey("access");
        props.getS3().setSecretKey("secret");
        props.getS3().setSessionToken("token");

        TestBuilders builders = mockBuilders();

        try (MockedStatic<S3Client> s3ClientStatic = mockStatic(S3Client.class);
             MockedStatic<S3Presigner> presignerStatic = mockStatic(S3Presigner.class)) {
            s3ClientStatic.when(S3Client::builder).thenReturn(builders.clientBuilder);
            presignerStatic.when(S3Presigner::builder).thenReturn(builders.presignerBuilder);

            FileStorage storage = new S3StorageConfig().s3Storage(props);

            assertThat(storage).isInstanceOf(S3FileStorage.class);

            AwsCredentialsProvider provider = captureCredentialsProvider(builders.clientBuilder);
            assertThat(provider).isInstanceOf(StaticCredentialsProvider.class);
            AwsCredentials credentials = provider.resolveCredentials();
            assertThat(credentials).isInstanceOf(AwsSessionCredentials.class);
            AwsSessionCredentials session = (AwsSessionCredentials) credentials;
            assertThat(session.accessKeyId()).isEqualTo("access");
            assertThat(session.secretAccessKey()).isEqualTo("secret");
            assertThat(session.sessionToken()).isEqualTo("token");

            verify(builders.presignerBuilder).credentialsProvider(provider);
        }
    }

    @Test
    void createsStorageUsingProfileCredentialsWhenProfileConfigured() {
        StorageProps props = storageProps();
        props.getS3().setProfile("profile");

        TestBuilders builders = mockBuilders();

        try (MockedStatic<S3Client> s3ClientStatic = mockStatic(S3Client.class);
             MockedStatic<S3Presigner> presignerStatic = mockStatic(S3Presigner.class)) {
            s3ClientStatic.when(S3Client::builder).thenReturn(builders.clientBuilder);
            presignerStatic.when(S3Presigner::builder).thenReturn(builders.presignerBuilder);

            FileStorage storage = new S3StorageConfig().s3Storage(props);

            assertThat(storage).isInstanceOf(S3FileStorage.class);

            AwsCredentialsProvider provider = captureCredentialsProvider(builders.clientBuilder);
            assertThat(provider).isInstanceOf(ProfileCredentialsProvider.class);

            verify(builders.presignerBuilder).credentialsProvider(provider);
        }
    }

    @Test
    void createsStorageUsingDefaultCredentialsWhenNothingConfigured() {
        StorageProps props = storageProps();

        TestBuilders builders = mockBuilders();

        try (MockedStatic<S3Client> s3ClientStatic = mockStatic(S3Client.class);
             MockedStatic<S3Presigner> presignerStatic = mockStatic(S3Presigner.class)) {
            s3ClientStatic.when(S3Client::builder).thenReturn(builders.clientBuilder);
            presignerStatic.when(S3Presigner::builder).thenReturn(builders.presignerBuilder);

            FileStorage storage = new S3StorageConfig().s3Storage(props);

            assertThat(storage).isInstanceOf(S3FileStorage.class);

            AwsCredentialsProvider provider = captureCredentialsProvider(builders.clientBuilder);
            assertThat(provider).isInstanceOf(DefaultCredentialsProvider.class);

            verify(builders.presignerBuilder).credentialsProvider(provider);
        }
    }

    private StorageProps storageProps() {
        StorageProps props = new StorageProps();
        props.getS3().setRegion("us-east-1");
        return props;
    }

    private AwsCredentialsProvider captureCredentialsProvider(S3ClientBuilder builder) {
        ArgumentCaptor<AwsCredentialsProvider> captor = ArgumentCaptor.forClass(AwsCredentialsProvider.class);
        verify(builder).credentialsProvider(captor.capture());
        return captor.getValue();
    }

    private TestBuilders mockBuilders() {
        S3ClientBuilder clientBuilder = mock(S3ClientBuilder.class, RETURNS_SELF);
        S3Presigner.Builder presignerBuilder = mock(S3Presigner.Builder.class, RETURNS_SELF);
        S3Client client = mock(S3Client.class);
        S3Presigner presigner = mock(S3Presigner.class);

        org.mockito.Mockito.when(clientBuilder.build()).thenReturn(client);
        org.mockito.Mockito.when(presignerBuilder.build()).thenReturn(presigner);
        org.mockito.Mockito.when(clientBuilder.region(any(Region.class))).thenReturn(clientBuilder);
        org.mockito.Mockito.when(presignerBuilder.region(any(Region.class))).thenReturn(presignerBuilder);

        return new TestBuilders(clientBuilder, presignerBuilder);
    }

    private record TestBuilders(S3ClientBuilder clientBuilder, S3Presigner.Builder presignerBuilder) { }
}
