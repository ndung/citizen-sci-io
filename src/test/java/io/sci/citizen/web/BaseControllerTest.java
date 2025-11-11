package io.sci.citizen.web;

import io.sci.citizen.model.Project;
import io.sci.citizen.model.User;
import io.sci.citizen.service.ProjectService;
import io.sci.citizen.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BaseControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private ProjectService projectService;

    private BaseController controller;

    @BeforeEach
    void setUp() {
        controller = new BaseController();
        controller.userService = userService;
        controller.projectService = projectService;
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void isAuthorizedReturnsTrueForAdmins() {
        setAuthentication("admin", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        boolean result = controller.isAuthorized(new Project());

        assertThat(result).isTrue();
        verifyNoInteractions(userService, projectService);
    }

    @Test
    void isAuthorizedReturnsFalseWhenProjectIsNullAndUserNotAdmin() {
        setAuthentication("user", List.of(new SimpleGrantedAuthority("ROLE_USER")));

        boolean result = controller.isAuthorized((Project) null);

        assertThat(result).isFalse();
    }

    @Test
    void isAuthorizedReturnsTrueWhenUserCreatedProject() {
        setAuthentication("creator", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        User user = new User();
        user.setId(1L);
        when(userService.getUser("creator")).thenReturn(user);

        Project project = new Project();
        User creator = new User();
        creator.setId(1L);
        project.setCreator(creator);

        boolean result = controller.isAuthorized(project);

        assertThat(result).isTrue();
        verify(userService).getUser("creator");
    }

    @Test
    void isAuthorizedReturnsFalseWhenDifferentCreator() {
        setAuthentication("viewer", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        User user = new User();
        user.setId(1L);
        when(userService.getUser("viewer")).thenReturn(user);

        Project project = new Project();
        User creator = new User();
        creator.setId(2L);
        project.setCreator(creator);

        boolean result = controller.isAuthorized(project);

        assertThat(result).isFalse();
    }

    @Test
    void isAuthorizedWithIdDelegatesToProjectService() {
        setAuthentication("creator", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        User user = new User();
        user.setId(3L);
        when(userService.getUser("creator")).thenReturn(user);

        Project project = new Project();
        User creator = new User();
        creator.setId(3L);
        project.setCreator(creator);
        when(projectService.getById(10L)).thenReturn(project);

        boolean result = controller.isAuthorized(10L);

        assertThat(result).isTrue();
        verify(projectService).getById(10L);
    }

    @Test
    void getUsernameReturnsAuthenticationName() {
        setAuthentication("alice", List.of(new SimpleGrantedAuthority("ROLE_USER")));

        String username = controller.getUsername();

        assertThat(username).isEqualTo("alice");
    }

    @Test
    void getUsernameReturnsNullWhenNoAuthentication() {
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());

        String username = controller.getUsername();

        assertThat(username).isNull();
    }

    @Test
    void getUserReturnsUserFromService() {
        setAuthentication("bob", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        User expected = new User();
        when(userService.getUser("bob")).thenReturn(expected);

        User user = controller.getUser();

        assertThat(user).isSameAs(expected);
        verify(userService).getUser("bob");
    }

    @Test
    void getUserReturnsNullWhenNoUsername() {
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());

        User user = controller.getUser();

        assertThat(user).isNull();
        verifyNoInteractions(userService);
    }

    @Test
    void getUserByUsernameReturnsNullWhenUsernameNull() {
        User user = controller.getUser((String) null);

        assertThat(user).isNull();
        verifyNoInteractions(userService);
    }

    @Test
    void getUserByUsernameReturnsUserWhenUsernameProvided() {
        User expected = new User();
        when(userService.getUser("carol")).thenReturn(expected);

        User user = controller.getUser("carol");

        assertThat(user).isSameAs(expected);
        verify(userService).getUser("carol");
    }

    private void setAuthentication(String username, Collection<? extends GrantedAuthority> authorities) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(username, "password", authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }
}
