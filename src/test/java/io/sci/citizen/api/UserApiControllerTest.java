package io.sci.citizen.api;

import io.sci.citizen.model.User;
import io.sci.citizen.model.dto.ChangePasswordRequest;
import io.sci.citizen.model.dto.UserRequest;
import io.sci.citizen.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserApiControllerTest {

    @Mock
    private UserService userService;

    @Spy
    @InjectMocks
    private UserApiController controller;

    @Test
    void changePasswordReturnsForbiddenWhenAuthorizationFails() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        doReturn(false).when(controller).authorize("token");

        ResponseEntity<Response> result = controller.changePassword("token", request);

        assertThat(result).isSameAs(BaseApiController.FORBIDDEN);
        verifyNoInteractions(userService);
    }

    @Test
    void changePasswordUpdatesUserAndReturnsSuccessResponse() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("old");
        request.setNewPassword("new");
        User user = new User();

        doReturn(true).when(controller).authorize("token");
        doReturn("42").when(controller).getUserId("token");
        when(userService.getById(42L)).thenReturn(user);

        ResponseEntity<Response> result = controller.changePassword("token", request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData()).isSameAs(user);
        verify(userService).changePassword(user, "old", "new");
    }

    @Test
    void changePasswordReturnsBadRequestWhenServiceThrows() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        doReturn(true).when(controller).authorize("token");
        doReturn("1").when(controller).getUserId("token");
        when(userService.getById(1L)).thenThrow(new IllegalStateException("boom"));

        ResponseEntity<Response> result = controller.changePassword("token", request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData()).isNull();
        assertThat(result.getBody().getMessage()).isEqualTo("boom");
    }

    @Test
    void changeProfileReturnsForbiddenWhenAuthorizationFails() {
        UserRequest request = new UserRequest();
        doReturn(false).when(controller).authorize("token");

        ResponseEntity<Response> result = controller.changeProfile("token", request);

        assertThat(result).isSameAs(BaseApiController.FORBIDDEN);
        verifyNoInteractions(userService);
    }

    @Test
    void changeProfileUpdatesUserAndReturnsSuccessResponse() {
        UserRequest request = new UserRequest();
        User user = new User();

        doReturn(true).when(controller).authorize("token");
        doReturn("7").when(controller).getUserId("token");
        when(userService.getById(7L)).thenReturn(user);
        when(userService.updateProfile(user, request)).thenReturn(user);

        ResponseEntity<Response> result = controller.changeProfile("token", request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData()).isSameAs(user);
        verify(userService).updateProfile(user, request);
    }

    @Test
    void changeProfileReturnsBadRequestWhenServiceThrows() {
        UserRequest request = new UserRequest();
        doReturn(true).when(controller).authorize("token");
        doReturn("12").when(controller).getUserId("token");
        when(userService.getById(anyLong())).thenThrow(new RuntimeException("failure"));

        ResponseEntity<Response> result = controller.changeProfile("token", request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData()).isNull();
        assertThat(result.getBody().getMessage()).isEqualTo("failure");
    }
}
