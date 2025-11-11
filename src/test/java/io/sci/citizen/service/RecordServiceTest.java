package io.sci.citizen.service;

import io.sci.citizen.config.FileStorage;
import io.sci.citizen.model.Data;
import io.sci.citizen.model.Image;
import io.sci.citizen.model.Project;
import io.sci.citizen.model.User;
import io.sci.citizen.model.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordServiceTest {

    @Mock
    private DataRepository dataRepo;

    @Mock
    private ImageRepository imageRepo;

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private RecordService recordService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(recordService, "userRepo", userRepo);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getRecordsSummaryByUserReturnsCounts() {
        when(dataRepo.getRecordCountByUserId(5L)).thenReturn(7);
        when(dataRepo.getRecordCountByUserIdAndStatus(5L, 1)).thenReturn(3);
        when(dataRepo.getRecordCount()).thenReturn(42);

        int[] summary = recordService.getRecordsSummaryByUser(5L);

        assertThat(summary).containsExactly(7, 3, 42);
    }

    @Test
    void getRecordsByUserTypeTwoReturnsAllRecords() {
        List<Data> records = List.of(new Data(), new Data());
        when(dataRepo.findAll()).thenReturn(records);

        List<Data> result = recordService.getRecordsByUser(99L, 2);

        assertThat(result).isEqualTo(records);
        verify(dataRepo).findAll();
        verify(dataRepo, never()).findByUser_IdOrderByCreatedAtDesc(anyLong());
        verify(dataRepo, never()).findByUser_IdAndStatusOrderByCreatedAtDesc(anyLong(), anyInt());
    }

    @Test
    void getRecordsByUserTypeOneReturnsVerifiedRecords() {
        List<Data> verified = List.of(new Data());
        when(dataRepo.findByUser_IdAndStatusOrderByCreatedAtDesc(12L, 1)).thenReturn(verified);

        List<Data> result = recordService.getRecordsByUser(12L, 1);

        assertThat(result).isEqualTo(verified);
        verify(dataRepo).findByUser_IdAndStatusOrderByCreatedAtDesc(12L, 1);
        verify(dataRepo, never()).findAll();
        verify(dataRepo, never()).findByUser_IdOrderByCreatedAtDesc(anyLong());
    }

    @Test
    void getRecordsByUserDefaultReturnsAllUserRecords() {
        List<Data> all = List.of(new Data(), new Data(), new Data());
        when(dataRepo.findByUser_IdOrderByCreatedAtDesc(8L)).thenReturn(all);

        List<Data> result = recordService.getRecordsByUser(8L, 0);

        assertThat(result).isEqualTo(all);
        verify(dataRepo).findByUser_IdOrderByCreatedAtDesc(8L);
        verify(dataRepo, never()).findAll();
        verify(dataRepo, never()).findByUser_IdAndStatusOrderByCreatedAtDesc(anyLong(), anyInt());
    }

    @Test
    void getRecordsSummaryByUserAndProjectReturnsCounts() {
        when(dataRepo.getRecordCountByProjectIdAndUserId(3L, 4L)).thenReturn(6);
        when(dataRepo.getRecordCountByProjectIdAndUserIdAndStatus(3L, 4L, 1)).thenReturn(2);
        when(dataRepo.getRecordCountByProjectId(3L)).thenReturn(11);

        int[] summary = recordService.getRecordsSummaryByUserAndProject(4L, 3L);

        assertThat(summary).containsExactly(6, 2, 11);
    }

    @Test
    void getRecordsByUserAndProjectTypeTwoReturnsAllProjectRecords() {
        List<Data> records = List.of(new Data());
        when(dataRepo.findByProject_IdOrderByCreatedAtDesc(15L)).thenReturn(records);

        List<Data> result = recordService.getRecordsByUserAndProject(2L, 15L, 2);

        assertThat(result).isEqualTo(records);
        verify(dataRepo).findByProject_IdOrderByCreatedAtDesc(15L);
        verify(dataRepo, never()).findByProject_IdAndUser_IdOrderByCreatedAtDesc(anyLong(), anyLong());
        verify(dataRepo, never()).findByProject_IdAndUser_IdAndStatusOrderByCreatedAtDesc(anyLong(), anyLong(), anyInt());
    }

    @Test
    void getRecordsByUserAndProjectTypeOneReturnsVerifiedUserRecords() {
        List<Data> verified = List.of(new Data());
        when(dataRepo.findByProject_IdAndUser_IdAndStatusOrderByCreatedAtDesc(9L, 10L, 1)).thenReturn(verified);

        List<Data> result = recordService.getRecordsByUserAndProject(10L, 9L, 1);

        assertThat(result).isEqualTo(verified);
        verify(dataRepo).findByProject_IdAndUser_IdAndStatusOrderByCreatedAtDesc(9L, 10L, 1);
        verify(dataRepo, never()).findByProject_IdOrderByCreatedAtDesc(anyLong());
        verify(dataRepo, never()).findByProject_IdAndUser_IdOrderByCreatedAtDesc(anyLong(), anyLong());
    }

    @Test
    void getRecordsByUserAndProjectDefaultReturnsAllUserProjectRecords() {
        List<Data> all = List.of(new Data(), new Data());
        when(dataRepo.findByProject_IdAndUser_IdOrderByCreatedAtDesc(20L, 21L)).thenReturn(all);

        List<Data> result = recordService.getRecordsByUserAndProject(21L, 20L, 0);

        assertThat(result).isEqualTo(all);
        verify(dataRepo).findByProject_IdAndUser_IdOrderByCreatedAtDesc(20L, 21L);
        verify(dataRepo, never()).findByProject_IdOrderByCreatedAtDesc(anyLong());
        verify(dataRepo, never()).findByProject_IdAndUser_IdAndStatusOrderByCreatedAtDesc(anyLong(), anyLong(), anyInt());
    }

    @Test
    void getByIdThrowsWhenImageMissing() {
        when(imageRepo.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordService.getById(1L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void getByIdReturnsImageForAdmin() {
        authenticate("admin", "ROLE_ADMIN");

        Image image = new Image();
        Data data = new Data();
        Project project = new Project();
        User creator = new User();
        creator.setId(100L);
        project.setCreator(creator);
        data.setProject(project);
        image.setData(data);
        when(imageRepo.findById(5L)).thenReturn(Optional.of(image));

        Image result = recordService.getById(5L);

        assertThat(result).isSameAs(image);
    }

    @Test
    void getByIdThrowsForbiddenWhenUserIsNotCreator() {
        authenticate("user", "ROLE_USER");

        User requester = new User();
        requester.setId(1L);
        when(userRepo.findByUsername("user")).thenReturn(Optional.of(requester));

        Image image = new Image();
        Data data = new Data();
        Project project = new Project();
        User creator = new User();
        creator.setId(2L);
        project.setCreator(creator);
        data.setProject(project);
        image.setData(data);
        when(imageRepo.findById(9L)).thenReturn(Optional.of(image));

        assertThatThrownBy(() -> recordService.getById(9L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void getByIdReturnsImageWhenUserIsCreator() {
        authenticate("owner", "ROLE_USER");

        User requester = new User();
        requester.setId(55L);
        when(userRepo.findByUsername("owner")).thenReturn(Optional.of(requester));

        Image image = new Image();
        Data data = new Data();
        Project project = new Project();
        User creator = new User();
        creator.setId(55L);
        project.setCreator(creator);
        data.setProject(project);
        image.setData(data);
        when(imageRepo.findById(11L)).thenReturn(Optional.of(image));

        Image result = recordService.getById(11L);

        assertThat(result).isSameAs(image);
    }

    @Test
    void updateStatusThrowsWhenImageMissing() {
        when(imageRepo.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordService.updateStatus(7L, 3))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void updateStatusUpdatesImageForAdmin() {
        authenticate("admin", "ROLE_ADMIN");

        Image image = new Image();
        Data data = new Data();
        Project project = new Project();
        User creator = new User();
        creator.setId(200L);
        project.setCreator(creator);
        data.setProject(project);
        image.setData(data);
        when(imageRepo.findById(4L)).thenReturn(Optional.of(image));

        recordService.updateStatus(4L, 2);

        assertThat(image.getStatus()).isEqualTo(2);
        verify(imageRepo).save(image);
    }

    private void authenticate(String username, String... roles) {
        var authorities = Arrays.stream(roles)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        var auth = new UsernamePasswordAuthenticationToken(username, "password", authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
