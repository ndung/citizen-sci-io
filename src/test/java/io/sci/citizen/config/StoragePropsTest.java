package io.sci.citizen.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StoragePropsTest {

    @Test
    void defaultsAreInitialized() {
        StorageProps props = new StorageProps();

        assertThat(props.getType()).isNull();
        assertThat(props.getLocal()).isNotNull();
        assertThat(props.getS3()).isNotNull();
        assertThat(props.getS3().getPrefix()).isEqualTo("uploads/");
        assertThat(props.getS3().getUrlMinutes()).isEqualTo(10);
    }

    @Test
    void canOverrideTopLevelProperties() {
        StorageProps props = new StorageProps();
        StorageProps.Local local = new StorageProps.Local();
        local.setBasePath("/var/data");
        local.setBaseUrl("https://example.test/files");

        StorageProps.S3 s3 = new StorageProps.S3();
        s3.setBucket("bucket-name");
        s3.setRegion("us-west-2");
        s3.setPrefix("custom/");
        s3.setUrlMinutes(30);
        s3.setAccessKey("access");
        s3.setSecretKey("secret");
        s3.setSessionToken("token");
        s3.setProfile("profile");

        props.setType("custom");
        props.setLocal(local);
        props.setS3(s3);

        assertThat(props.getType()).isEqualTo("custom");
        assertThat(props.getLocal()).isSameAs(local);
        assertThat(props.getLocal().getBasePath()).isEqualTo("/var/data");
        assertThat(props.getLocal().getBaseUrl()).isEqualTo("https://example.test/files");
        assertThat(props.getS3()).isSameAs(s3);
        assertThat(props.getS3().getBucket()).isEqualTo("bucket-name");
        assertThat(props.getS3().getRegion()).isEqualTo("us-west-2");
        assertThat(props.getS3().getPrefix()).isEqualTo("custom/");
        assertThat(props.getS3().getUrlMinutes()).isEqualTo(30);
        assertThat(props.getS3().getAccessKey()).isEqualTo("access");
        assertThat(props.getS3().getSecretKey()).isEqualTo("secret");
        assertThat(props.getS3().getSessionToken()).isEqualTo("token");
        assertThat(props.getS3().getProfile()).isEqualTo("profile");
    }

    @Test
    void nestedPropertyMutatorsApplyToDefaultInstances() {
        StorageProps props = new StorageProps();

        props.getLocal().setBasePath("/tmp");
        props.getLocal().setBaseUrl("http://localhost:8080/files");
        props.getS3().setBucket("bucket");
        props.getS3().setRegion("eu-central-1");

        assertThat(props.getLocal().getBasePath()).isEqualTo("/tmp");
        assertThat(props.getLocal().getBaseUrl()).isEqualTo("http://localhost:8080/files");
        assertThat(props.getS3().getBucket()).isEqualTo("bucket");
        assertThat(props.getS3().getRegion()).isEqualTo("eu-central-1");
    }
}
