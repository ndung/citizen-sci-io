package io.sci.citizen.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
@EnableConfigurationProperties(StorageProps.class)
public class LocalStorageConfig implements WebMvcConfigurer {

    private final StorageProps props;
    public LocalStorageConfig(StorageProps props) { this.props = props; }

    // Serve files under /files/** directly from disk
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + Paths.get(props.getLocal().getBasePath()).toAbsolutePath() + "/";
        registry.addResourceHandler("/files/**").addResourceLocations(location);
    }

    @Bean
    @ConditionalOnProperty(name = "app.storage.type", havingValue = "local")
    public FileStorage localStorage() {
        return new LocalFileStorage(props);
    }
}
