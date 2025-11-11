package io.sci.citizen.service;

import io.sci.citizen.model.Project;
import io.sci.citizen.model.User;
import io.sci.citizen.model.dto.ProjectRequest;
import io.sci.citizen.model.repository.ProjectRepository;
import io.sci.citizen.model.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(projectService, "userRepo", userRepository);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void findAllReturnsAllProjectsForAdmin() {
        var auth = new UsernamePasswordAuthenticationToken(
                "admin",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        var projects = List.of(new Project(), new Project());
        when(projectRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))).thenReturn(projects);

        List<Project> result = projectService.findAll();

        assertThat(result).isEqualTo(projects);
        verify(projectRepository).findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        verifyNoInteractions(userRepository);
    }

    @Test
    void findAllReturnsProjectsForAuthenticatedUser() {
        var auth = new UsernamePasswordAuthenticationToken(
                "jane",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        User user = new User();
        user.setId(7L);
        when(userRepository.findByUsername("jane")).thenReturn(Optional.of(user));

        var projects = List.of(new Project(), new Project());
        when(projectRepository.findProjectsByCreator_Id(7L)).thenReturn(projects);

        List<Project> result = projectService.findAll();

        assertThat(result).isEqualTo(projects);
        verify(projectRepository).findProjectsByCreator_Id(7L);
    }

    @Test
    void findAllReturnsNullWhenUserMissing() {
        var auth = new UsernamePasswordAuthenticationToken(
                "missing",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        List<Project> result = projectService.findAll();

        assertThat(result).isNull();
        verify(projectRepository, never()).findProjectsByCreator_Id(any());
    }

    @Test
    void findAllByUserIdCombinesPublicAndPrivateProjects() {
        Project publicProject = new Project();
        publicProject.setId(1L);
        Project sharedProject = new Project();
        sharedProject.setId(2L);
        Project privateProject = new Project();
        privateProject.setId(3L);
        Project duplicateFromPrivate = new Project();
        duplicateFromPrivate.setId(2L);

        when(projectRepository.findProjectsByPubliclyAvailable(true))
                .thenReturn(List.of(publicProject, sharedProject));
        when(projectRepository.findProjectsByCreator_Id(5L))
                .thenReturn(List.of(privateProject, duplicateFromPrivate));

        Map<Long, Project> result = projectService.findAll(5L);

        assertThat(result)
                .containsEntry(1L, publicProject)
                .containsEntry(2L, sharedProject)
                .containsEntry(3L, privateProject)
                .hasSize(3);
    }

    @Test
    void createSetsCreatorFromAuthenticatedUser() {
        var auth = new UsernamePasswordAuthenticationToken(
                "jane",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        User user = new User();
        when(userRepository.findByUsername("jane")).thenReturn(Optional.of(user));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectRequest request = new ProjectRequest();
        request.setName(" Project ");
        request.setIcon(" icon ");
        request.setDescription("desc");
        request.setEnabled(true);
        request.setPubliclyAvailable(true);

        Project result = projectService.create(request);

        assertThat(result.getCreator()).isEqualTo(user);
        assertThat(result.getName()).isEqualTo("Project");
        assertThat(result.getIcon()).isEqualTo("icon");
        assertThat(result.getDescription()).isEqualTo("desc");
        assertThat(result.isPubliclyAvailable()).isTrue();
    }

    @Test
    void getByIdReturnsProjectWhenPresent() {
        Project project = new Project();
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

        Project result = projectService.getById(10L);

        assertThat(result).isSameAs(project);
    }

    @Test
    void getByIdThrowsWhenMissing() {
        when(projectRepository.findById(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getById(20L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void updateAppliesRequestChanges() {
        Project existing = new Project();
        existing.setName("Old");
        existing.setIcon("old");
        existing.setDescription("old");
        existing.setEnabled(false);
        existing.setPubliclyAvailable(false);

        when(projectRepository.findById(30L)).thenReturn(Optional.of(existing));
        when(projectRepository.save(existing)).thenReturn(existing);

        ProjectRequest request = new ProjectRequest();
        request.setName("New");
        request.setIcon("new-icon");
        request.setDescription("new-desc");
        request.setEnabled(true);
        request.setPubliclyAvailable(true);

        Project result = projectService.update(30L, request);

        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getIcon()).isEqualTo("new-icon");
        assertThat(result.getDescription()).isEqualTo("new-desc");
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.isPubliclyAvailable()).isTrue();
        verify(projectRepository).save(existing);
    }
}
