package io.sci.citizen.api;

import io.sci.citizen.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectApiControllerTest {

    @Mock
    private ProjectService projectService;

    @Spy
    @InjectMocks
    private ProjectApiController controller;

    @Test
    void getReturnsForbiddenWhenAuthorizationFails() {
        String token = "Bearer some-token";
        doReturn(false).when(controller).authorize(token);

        ResponseEntity<Response> response = controller.get(token);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNull();
        verify(controller).authorize(token);
        verify(controller, never()).getUserId(anyString());
        verifyNoInteractions(projectService);
    }

    @Test
    void getReturnsProjectsWhenAuthorized() {
        String token = "Bearer valid-token";
        doReturn(true).when(controller).authorize(token);
        doReturn("42").when(controller).getUserId(token);
        Map<Long, String> projects = Map.of(1L, "Project A");
        when(projectService.findAll(42L)).thenReturn(projects);

        ResponseEntity<Response> response = controller.get(token);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(projects);
        verify(controller).getUserId(token);
        verify(projectService).findAll(42L);
    }

    @Test
    void getReturnsBadRequestWhenServiceThrows() {
        String token = "Bearer bad-token";
        doReturn(true).when(controller).authorize(token);
        doReturn("7").when(controller).getUserId(token);
        when(projectService.findAll(7L)).thenThrow(new RuntimeException("boom"));

        ResponseEntity<Response> response = controller.get(token);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isNull();
        assertThat(response.getBody().getMessage()).isEqualTo("boom");
        verify(controller).getUserId(token);
    }
}
