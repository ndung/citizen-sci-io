package io.sci.citizen.web;

import io.sci.citizen.config.FileStorage;
import io.sci.citizen.config.StoredFile;
import io.sci.citizen.model.Project;
import io.sci.citizen.model.dto.ProjectRequest;
import io.sci.citizen.service.ProjectService;
import io.sci.citizen.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    @Mock
    private FileStorage storage;

    @Mock
    private ProjectService projectService;

    @Mock
    private UserService userService;

    private ProjectController controller;

    @BeforeEach
    void setUp() {
        controller = Mockito.spy(new ProjectController());
        ReflectionTestUtils.setField(controller, "storage", storage);
        ReflectionTestUtils.setField(controller, "projectService", projectService);
        ReflectionTestUtils.setField(controller, "userService", userService);
    }

    @Test
    void create_withValidFormAndIcon_storesFileAndRedirects() throws Exception {
        ProjectRequest form = new ProjectRequest();
        form.setName("Example");
        BindingResult binding = mock(BindingResult.class);
        when(binding.hasErrors()).thenReturn(false);

        MultipartFile iconFile = mock(MultipartFile.class);
        when(iconFile.isEmpty()).thenReturn(false);
        when(iconFile.getContentType()).thenReturn("image/png");
        when(iconFile.getOriginalFilename()).thenReturn("logo.png");

        when(storage.store(anyString(), eq(iconFile)))
                .thenReturn(new StoredFile("stored-key", new URI("https://example.com/icon.png"), 10L, "image/png"));
        when(projectService.create(form)).thenReturn(new Project());

        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        Model model = new ExtendedModelMap();

        String view = controller.create(form, binding, iconFile, redirectAttributes, model);

        assertThat(view).isEqualTo("redirect:/projects");
        assertThat(form.getIcon()).isEqualTo("stored-key");
        assertThat(redirectAttributes.getFlashAttributes()).containsEntry("projectSaved", Boolean.TRUE);
        verify(storage).store(startsWith("icon_"), eq(iconFile));
        verify(projectService).create(form);
    }

    @Test
    void create_withBindingErrors_returnsProjectsView() throws Exception {
        ProjectRequest form = new ProjectRequest();
        BindingResult binding = mock(BindingResult.class);
        when(binding.hasErrors()).thenReturn(true);
        when(projectService.findAll()).thenReturn(List.of());

        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        Model model = new ExtendedModelMap();

        String view = controller.create(form, binding, null, redirectAttributes, model);

        assertThat(view).isEqualTo("projects");
        assertThat(model.getAttribute("projects")).isEqualTo(List.of());
        verify(projectService, never()).create(any(ProjectRequest.class));
    }

    @Test
    void create_withInvalidIconType_rejectsBinding() throws Exception {
        ProjectRequest form = new ProjectRequest();
        form.setName("Example");
        BindingResult binding = mock(BindingResult.class);
        when(binding.hasErrors()).thenReturn(false);
        when(projectService.findAll()).thenReturn(List.of());

        MultipartFile iconFile = mock(MultipartFile.class);
        when(iconFile.isEmpty()).thenReturn(false);
        when(iconFile.getContentType()).thenReturn("image/gif");
        when(iconFile.getOriginalFilename()).thenReturn("logo.gif");

        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        Model model = new ExtendedModelMap();

        String view = controller.create(form, binding, iconFile, redirectAttributes, model);

        assertThat(view).isEqualTo("projects");
        verify(binding).rejectValue(eq("icon"), eq("icon.invalid"), anyString());
        assertThat(model.getAttribute("projects")).isEqualTo(List.of());
        verify(projectService, never()).create(any(ProjectRequest.class));
        verify(storage, never()).store(anyString(), any(MultipartFile.class));
    }

    @Test
    void list_withProjectId_populatesFormFromService() {
        Project project = new Project();
        project.setId(5L);
        project.setName("Project name");
        project.setIcon("icon.png");
        project.setDescription("Description");
        project.setEnabled(false);
        project.setPubliclyAvailable(true);

        when(projectService.getById(5L)).thenReturn(project);
        when(projectService.findAll()).thenReturn(List.of(project));

        Model model = new ExtendedModelMap();

        String view = controller.list(5L, model);

        assertThat(view).isEqualTo("projects");
        assertThat(model.getAttribute("projects")).isEqualTo(List.of(project));
        ProjectRequest form = (ProjectRequest) model.getAttribute("project");
        assertThat(form).isNotNull();
        assertThat(form.getId()).isEqualTo(5L);
        assertThat(form.getName()).isEqualTo("Project name");
        assertThat(form.getIcon()).isEqualTo("icon.png");
        assertThat(form.getDescription()).isEqualTo("Description");
        assertThat(form.isEnabled()).isFalse();
        assertThat(form.isPubliclyAvailable()).isTrue();
    }

    @Test
    void update_whenNotAuthorized_throwsForbidden() {
        ProjectRequest form = new ProjectRequest();
        BindingResult binding = mock(BindingResult.class);
        when(binding.hasErrors()).thenReturn(false);
        doReturn(false).when(controller).isAuthorized(42L);

        assertThatThrownBy(() -> controller.update(42L, form, binding, null,
                new RedirectAttributesModelMap(), new ExtendedModelMap()))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(projectService, never()).update(anyLong(), any(ProjectRequest.class));
    }

    @Test
    void update_withIconFile_storesFileAndRedirects() throws Exception {
        ProjectRequest form = new ProjectRequest();
        form.setName("Example");
        BindingResult binding = mock(BindingResult.class);
        when(binding.hasErrors()).thenReturn(false);
        doReturn(true).when(controller).isAuthorized(7L);

        MultipartFile iconFile = mock(MultipartFile.class);
        when(iconFile.isEmpty()).thenReturn(false);
        when(iconFile.getContentType()).thenReturn("image/png");
        when(iconFile.getOriginalFilename()).thenReturn("logo.png");

        when(storage.store(anyString(), eq(iconFile)))
                .thenReturn(new StoredFile("stored-key", new URI("https://example.com/icon.png"), 10L, "image/png"));

        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        Model model = new ExtendedModelMap();

        String view = controller.update(7L, form, binding, iconFile, redirectAttributes, model);

        assertThat(view).isEqualTo("redirect:/projects");
        assertThat(form.getIcon()).isEqualTo("stored-key");
        assertThat(redirectAttributes.getFlashAttributes()).containsEntry("projectSaved", Boolean.TRUE);
        verify(storage).store(startsWith("icon_"), eq(iconFile));
        verify(projectService).update(7L, form);
    }

    @Test
    void update_whenStorageFails_returnsProjectsView() throws URISyntaxException, IOException {
        ProjectRequest form = new ProjectRequest();
        form.setName("Example");
        BindingResult binding = mock(BindingResult.class);
        when(binding.hasErrors()).thenReturn(false);
        doReturn(true).when(controller).isAuthorized(3L);

        MultipartFile iconFile = mock(MultipartFile.class);
        when(iconFile.isEmpty()).thenReturn(false);
        when(iconFile.getContentType()).thenReturn("image/png");
        when(iconFile.getOriginalFilename()).thenReturn("logo.png");

        when(storage.store(anyString(), eq(iconFile))).thenThrow(new IOException("boom"));
        when(projectService.findAll()).thenReturn(List.of());

        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        Model model = new ExtendedModelMap();

        String view = controller.update(3L, form, binding, iconFile, redirectAttributes, model);

        assertThat(view).isEqualTo("projects");
        verify(binding).rejectValue("icon", "icon.io", "Failed to store icon file.");
        assertThat(model.getAttribute("projects")).isEqualTo(List.of());
        verify(projectService, never()).update(anyLong(), any(ProjectRequest.class));
    }

    @Test
    void editForm_whenUnauthorized_throwsForbidden() {
        doReturn(false).when(controller).isAuthorized(9L);

        assertThatThrownBy(() -> controller.editForm(9L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void configForm_whenAuthorized_redirectsToSections() {
        doReturn(true).when(controller).isAuthorized(11L);

        String view = controller.configForm(11L);

        assertThat(view).isEqualTo("redirect:/sections?projectId=11");
    }

    @Test
    void configForm_whenUnauthorized_throwsForbidden() {
        doReturn(false).when(controller).isAuthorized(12L);

        assertThatThrownBy(() -> controller.configForm(12L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }
}
