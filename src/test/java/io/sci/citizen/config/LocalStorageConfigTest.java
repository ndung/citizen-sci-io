package io.sci.citizen.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LocalStorageConfigTest {

    @Test
    void addResourceHandlersRegistersLocalFileLocation() {
        StorageProps props = new StorageProps();
        StorageProps.Local local = new StorageProps.Local();
        local.setBasePath("uploads");
        props.setLocal(local);

        LocalStorageConfig config = new LocalStorageConfig(props);

        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration registration = mock(ResourceHandlerRegistration.class);
        when(registry.addResourceHandler("/files/**")).thenReturn(registration);
        when(registration.addResourceLocations(anyString())).thenReturn(registration);

        config.addResourceHandlers(registry);

        String expectedLocation = "file:" + Paths.get("uploads").toAbsolutePath() + "/";
        verify(registry).addResourceHandler("/files/**");
        verify(registration).addResourceLocations(expectedLocation);
    }

    @Test
    void localStorageCreatesLocalFileStorageAndEnsuresDirectoryExists(@TempDir Path tempDir) {
        StorageProps props = new StorageProps();
        StorageProps.Local local = new StorageProps.Local();
        Path basePath = tempDir.resolve("storage");
        local.setBasePath(basePath.toString());
        local.setBaseUrl("http://example.com/files/");
        props.setLocal(local);

        LocalStorageConfig config = new LocalStorageConfig(props);

        FileStorage storage = config.localStorage();

        assertThat(storage).isInstanceOf(LocalFileStorage.class);
        assertThat(Files.exists(basePath)).isTrue();
    }
}
