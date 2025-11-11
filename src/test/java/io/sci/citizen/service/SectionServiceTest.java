package io.sci.citizen.service;

import io.sci.citizen.model.Project;
import io.sci.citizen.model.Section;
import io.sci.citizen.model.User;
import io.sci.citizen.model.dto.SectionRequest;
import io.sci.citizen.model.repository.ProjectRepository;
import io.sci.citizen.model.repository.SectionRepository;
import io.sci.citizen.model.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SectionServiceTest {

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SectionService sectionService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listThrowsWhenProjectIdMissing() {
        assertThatThrownBy(() -> sectionService.list(Optional.empty()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));

        verifyNoInteractions(sectionRepository, projectRepository);
    }

    @Test
    void listReturnsProjectSectionsForAdmin() {
        setAuthentication("admin", "ROLE_ADMIN");
        List<Section> sections = List.of(new Section());
        when(sectionRepository.findByProject_IdOrderBySequenceAsc(5L)).thenReturn(sections);

        List<Section> result = sectionService.list(Optional.of(5L));

        assertThat(result).isEqualTo(sections);
        verify(sectionRepository).findByProject_IdOrderBySequenceAsc(5L);
        verifyNoInteractions(projectRepository, userRepository);
    }

    @Test
    void listAllowsProjectCreatorWhenNotAdmin() {
        setAuthentication("alice", "ROLE_USER");
        User user = new User();
        user.setId(42L);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        Project project = new Project();
        User creator = new User();
        creator.setId(42L);
        project.setCreator(creator);
        when(projectRepository.findById(7L)).thenReturn(Optional.of(project));

        List<Section> sections = List.of(new Section());
        when(sectionRepository.findByProject_IdOrderBySequenceAsc(7L)).thenReturn(sections);

        List<Section> result = sectionService.list(Optional.of(7L));

        assertThat(result).isEqualTo(sections);
    }

    @Test
    void listThrowsForbiddenWhenUserIsNotCreator() {
        setAuthentication("bob", "ROLE_USER");
        User user = new User();
        user.setId(10L);
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));

        Project project = new Project();
        User creator = new User();
        creator.setId(99L);
        project.setCreator(creator);
        when(projectRepository.findById(3L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> sectionService.list(Optional.of(3L)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));

        verify(sectionRepository, never()).findByProject_IdOrderBySequenceAsc(any());
    }

    @Test
    void createPersistsTrimmedSectionWithProject() {
        SectionRequest request = new SectionRequest();
        request.setSequence(3);
        request.setType(" text ");
        request.setName(" Survey Section ");
        request.setEnabled(false);
        request.setProjectId(11L);

        Project project = new Project();
        when(projectRepository.findById(11L)).thenReturn(Optional.of(project));
        when(sectionRepository.save(any(Section.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Section result = sectionService.create(request);

        assertThat(result.getSequence()).isEqualTo(3);
        assertThat(result.getType()).isEqualTo("text");
        assertThat(result.getName()).isEqualTo("Survey Section");
        assertThat(result.isEnabled()).isFalse();
        assertThat(result.getProject()).isSameAs(project);
    }

    @Test
    void createThrowsWhenProjectMissing() {
        SectionRequest request = new SectionRequest();
        request.setSequence(1);
        request.setType("type");
        request.setName("name");
        request.setProjectId(50L);

        when(projectRepository.findById(50L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sectionService.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));

        verify(sectionRepository, never()).save(any());
    }

    @Test
    void updateAppliesRequestToExistingSection() {
        Section existing = new Section();
        existing.setSequence(1);
        existing.setType("old");
        existing.setName("Old Name");
        existing.setEnabled(true);
        Project oldProject = new Project();
        existing.setProject(oldProject);

        when(sectionRepository.findById(4L)).thenReturn(Optional.of(existing));

        Project newProject = new Project();
        when(projectRepository.findById(9L)).thenReturn(Optional.of(newProject));
        when(sectionRepository.save(existing)).thenReturn(existing);

        SectionRequest request = new SectionRequest();
        request.setSequence(8);
        request.setType(" updated ");
        request.setName(" New Name ");
        request.setEnabled(false);
        request.setProjectId(9L);

        Section result = sectionService.update(4L, request);

        assertThat(result.getSequence()).isEqualTo(8);
        assertThat(result.getType()).isEqualTo("updated");
        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.isEnabled()).isFalse();
        assertThat(result.getProject()).isSameAs(newProject);
        verify(sectionRepository).save(existing);
    }

    @Test
    void updateClearsProjectWhenIdIsNull() {
        Section existing = new Section();
        Project project = new Project();
        existing.setProject(project);
        when(sectionRepository.findById(6L)).thenReturn(Optional.of(existing));
        when(sectionRepository.save(existing)).thenReturn(existing);

        SectionRequest request = new SectionRequest();
        request.setSequence(1);
        request.setType("type");
        request.setName("name");
        request.setEnabled(true);
        request.setProjectId(null);

        Section result = sectionService.update(6L, request);

        assertThat(result.getProject()).isNull();
    }

    @Test
    void deleteDelegatesToRepository() {
        sectionService.delete(12L);

        verify(sectionRepository).deleteById(12L);
    }

    private void setAuthentication(String username, String role) {
        var auth = new UsernamePasswordAuthenticationToken(
                username,
                "password",
                List.of(new SimpleGrantedAuthority(role))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
