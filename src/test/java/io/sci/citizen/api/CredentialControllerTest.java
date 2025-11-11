package io.sci.citizen.api;

import io.sci.citizen.api.component.JwtTokenUtil;
import io.sci.citizen.api.dto.LoginDetails;
import io.sci.citizen.api.dto.SignInRequest;
import io.sci.citizen.model.Project;
import io.sci.citizen.model.User;
import io.sci.citizen.model.dto.CreatePasswordRequest;
import io.sci.citizen.model.dto.UserRequest;
import io.sci.citizen.service.ProjectService;
import io.sci.citizen.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CredentialControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UserService userService;

    @Mock
    private ProjectService projectService;

    @Mock
    private JwtTokenUtil tokenUtil;

    @InjectMocks
    private CredentialController credentialController;

    @Test
    void checkUserNameReturnsNegativeOneWhenUserMissing() {
        when(userService.getUser("missing")).thenReturn(null);

        ResponseEntity<Response> response = credentialController.checkUserName("missing");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(-1);
        verify(userService).getUser("missing");
    }

    @Test
    void checkUserNameReturnsOneWhenUserEnabled() {
        User user = new User();
        user.setEnabled(true);
        when(userService.getUser("existing")).thenReturn(user);

        ResponseEntity<Response> response = credentialController.checkUserName("existing");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(1);
    }

    @Test
    void createPasswordReturnsLoginDetailsAndTokenWhenSuccessful() throws Exception {
        CreatePasswordRequest request = new CreatePasswordRequest();
        request.setUsername("alice");
        User user = new User();
        user.setId(42L);
        Map<Long, Project> projects = Map.of();

        when(userService.createPassword(request)).thenReturn(user);
        when(projectService.findAll(42L)).thenReturn(projects);
        when(tokenUtil.createToken(user)).thenReturn("jwt-token");

        ResponseEntity<Response> response = credentialController.createPassword(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst("Token")).isEqualTo("jwt-token");
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isInstanceOf(LoginDetails.class);
        LoginDetails details = (LoginDetails) response.getBody().getData();
        assertThat(details.user()).isSameAs(user);
        assertThat(details.projects()).isEqualTo(projects);
    }

    @Test
    void createPasswordReturnsBadRequestWhenServiceThrows() throws Exception {
        CreatePasswordRequest request = new CreatePasswordRequest();
        request.setUsername("alice");

        when(userService.createPassword(request)).thenThrow(new RuntimeException("boom"));

        ResponseEntity<Response> response = credentialController.createPassword(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("boom");
    }

    @Test
    void signInReturnsLoginDetailsWhenAuthenticationSucceeds() {
        SignInRequest request = new SignInRequest("alice", "password");
        User user = new User();
        user.setId(7L);
        Map<Long, Project> projects = Map.of();
        Authentication authentication = new UsernamePasswordAuthenticationToken("alice", "password");
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("alice")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userDetailsService.loadUserByUsername("alice")).thenReturn(userDetails);
        when(userService.getUser("alice")).thenReturn(user);
        when(projectService.findAll(7L)).thenReturn(projects);
        when(tokenUtil.createToken(user)).thenReturn("jwt-token");

        ResponseEntity<Response> response = credentialController.signIn(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst("Token")).isEqualTo("jwt-token");
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isInstanceOf(LoginDetails.class);
        LoginDetails details = (LoginDetails) response.getBody().getData();
        assertThat(details.user()).isSameAs(user);
        assertThat(details.projects()).isEqualTo(projects);
    }

    @Test
    void signInReturnsUnauthorizedWhenUserDetailsMissing() {
        SignInRequest request = new SignInRequest("bob", "password");
        Authentication authentication = new UsernamePasswordAuthenticationToken("bob", "password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userDetailsService.loadUserByUsername("bob")).thenReturn(null);

        ResponseEntity<Response> response = credentialController.signIn(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isNull();
    }

    @Test
    void signInReturnsBadRequestWhenAuthenticationFails() {
        SignInRequest request = new SignInRequest("carol", "password");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("auth failed"));

        ResponseEntity<Response> response = credentialController.signIn(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("auth failed");
    }

    @Test
    void signUpReturnsNegativeOneWhenUserServiceReturnsNull() {
        UserRequest request = new UserRequest();
        when(userService.signUp(request)).thenReturn(null);

        ResponseEntity<Response> response = credentialController.signUp(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(-1);
    }

    @Test
    void signUpReturnsOneWhenUserEnabled() {
        UserRequest request = new UserRequest();
        User user = new User();
        user.setEnabled(true);
        when(userService.signUp(request)).thenReturn(user);

        ResponseEntity<Response> response = credentialController.signUp(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(1);
    }
}
