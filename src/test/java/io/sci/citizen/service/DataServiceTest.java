package io.sci.citizen.service;

import io.sci.citizen.model.Data;
import io.sci.citizen.model.Project;
import io.sci.citizen.model.User;
import io.sci.citizen.model.repository.DataRepository;
import io.sci.citizen.model.repository.ProjectRepository;
import io.sci.citizen.model.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataServiceTest {

    @Mock
    private DataRepository dataRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    private DataService dataService;

    @BeforeEach
    void setUp() {
        dataService = new DataService(dataRepository, projectRepository);
        ReflectionTestUtils.setField(dataService, "userRepo", userRepository);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getProjectSummaryReturnsCounts() {
        when(dataRepository.getUserCountByProjectId(10L)).thenReturn(5);
        when(dataRepository.getRecordCountByProjectId(10L)).thenReturn(12);

        List<Integer> summary = dataService.getProjectSummary(10L);

        assertThat(summary).containsExactly(5, 12);
        verify(dataRepository).getUserCountByProjectId(10L);
        verify(dataRepository).getRecordCountByProjectId(10L);
    }

    @Test
    void findAllAsAdminReturnsSortedData() {
        mockAuthentication("admin", "ROLE_ADMIN");
        List<Data> expected = List.of(new Data());
        when(dataRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))).thenReturn(expected);

        List<Data> result = dataService.findAll();

        assertSame(expected, result);
        verify(dataRepository).findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Test
    void findAllAsNonAdminThrowsForbidden() {
        mockAuthentication("user", "ROLE_USER");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> dataService.findAll());

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(dataRepository, never()).findAll(any(Sort.class));
    }

    @Test
    void findAllByProjectAsAdminReturnsData() {
        mockAuthentication("admin", "ROLE_ADMIN");
        List<Data> expected = List.of(new Data());
        when(dataRepository.findByProject_IdOrderByCreatedAtDesc(2L)).thenReturn(expected);

        List<Data> result = dataService.findAll(2L);

        assertSame(expected, result);
        verify(dataRepository).findByProject_IdOrderByCreatedAtDesc(2L);
    }

    @Test
    void findAllByProjectAsNonAdminAndNotCreatorThrowsForbidden() {
        mockAuthentication("user", "ROLE_USER");
        User user = createUser(1L, "user");
        User other = createUser(2L, "other");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        Project project = new Project();
        project.setCreator(other);
        when(projectRepository.findById(99L)).thenReturn(Optional.of(project));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> dataService.findAll(99L));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(dataRepository, never()).findByProject_IdOrderByCreatedAtDesc(any());
    }

    @Test
    void findAllByProjectAsNonAdminAndCreatorReturnsData() {
        mockAuthentication("creator", "ROLE_USER");
        User creator = createUser(5L, "creator");
        when(userRepository.findByUsername("creator")).thenReturn(Optional.of(creator));
        Project project = new Project();
        project.setCreator(creator);
        when(projectRepository.findById(7L)).thenReturn(Optional.of(project));
        List<Data> expected = Collections.singletonList(new Data());
        when(dataRepository.findByProject_IdOrderByCreatedAtDesc(7L)).thenReturn(expected);

        List<Data> result = dataService.findAll(7L);

        assertSame(expected, result);
        verify(dataRepository).findByProject_IdOrderByCreatedAtDesc(7L);
    }

    @Test
    void getByIdMissingThrowsNotFound() {
        when(dataRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> dataService.getById(1L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void getByIdAsNonAdminNotCreatorThrowsForbidden() {
        mockAuthentication("user", "ROLE_USER");
        User user = createUser(1L, "user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        Project project = new Project();
        project.setCreator(createUser(2L, "other"));
        Data data = new Data();
        data.setProject(project);
        when(dataRepository.findById(3L)).thenReturn(Optional.of(data));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> dataService.getById(3L));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void getByIdAsNonAdminCreatorReturnsData() {
        mockAuthentication("creator", "ROLE_USER");
        User creator = createUser(4L, "creator");
        when(userRepository.findByUsername("creator")).thenReturn(Optional.of(creator));
        Project project = new Project();
        project.setCreator(creator);
        Data data = new Data();
        data.setProject(project);
        when(dataRepository.findById(11L)).thenReturn(Optional.of(data));

        Data result = dataService.getById(11L);

        assertSame(data, result);
    }

    @Test
    void updateStatusMissingThrowsNotFound() {
        when(dataRepository.findById(8L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> dataService.updateStatus(8L, 2));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void updateStatusAsNonAdminAndNotCreatorThrowsForbidden() {
        mockAuthentication("user", "ROLE_USER");
        User user = createUser(1L, "user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        Project project = new Project();
        project.setCreator(createUser(2L, "other"));
        Data data = new Data();
        data.setProject(project);
        when(dataRepository.findById(4L)).thenReturn(Optional.of(data));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> dataService.updateStatus(4L, 1));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(dataRepository, never()).save(any(Data.class));
    }

    @Test
    void updateStatusAsAdminUpdatesData() {
        mockAuthentication("admin", "ROLE_ADMIN");
        User admin = createUser(9L, "admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        Project project = new Project();
        project.setCreator(createUser(1L, "creator"));
        Data data = new Data();
        data.setProject(project);
        when(dataRepository.findById(6L)).thenReturn(Optional.of(data));

        dataService.updateStatus(6L, 3);

        ArgumentCaptor<Data> captor = ArgumentCaptor.forClass(Data.class);
        verify(dataRepository).save(captor.capture());
        Data saved = captor.getValue();
        assertSame(data, saved);
        assertEquals(3, saved.getStatus());
        assertSame(admin, saved.getVerificator());
        assertNotNull(saved.getVerifiedAt());
    }

    private void mockAuthentication(String username, String... roles) {
        List<GrantedAuthority> authorities = Arrays.stream(roles)
                .map(SimpleGrantedAuthority::new)
                .map(authority -> (GrantedAuthority) authority)
                .toList();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                username,
                "password",
                authorities
        );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    private User createUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }
}
