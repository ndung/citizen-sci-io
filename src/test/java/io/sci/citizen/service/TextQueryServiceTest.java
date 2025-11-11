package io.sci.citizen.service;

import io.sci.citizen.model.Project;
import io.sci.citizen.model.QueryOption;
import io.sci.citizen.model.Section;
import io.sci.citizen.model.TextQuery;
import io.sci.citizen.model.User;
import io.sci.citizen.model.dto.QueryOptionRequest;
import io.sci.citizen.model.dto.TextQueryRequest;
import io.sci.citizen.model.repository.QueryOptionRepository;
import io.sci.citizen.model.repository.SectionRepository;
import io.sci.citizen.model.repository.TextQueryRepository;
import io.sci.citizen.model.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TextQueryServiceTest {

    @Mock
    private TextQueryRepository textQueryRepository;

    @Mock
    private QueryOptionRepository queryOptionRepository;

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TextQueryService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "userRepo", userRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listThrowsWhenSectionIdMissing() {
        assertThatThrownBy(() -> service.list(null))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));

        verifyNoInteractions(sectionRepository, textQueryRepository);
    }

    @Test
    void listThrowsWhenSectionNotFound() {
        when(sectionRepository.findById(15L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.list(15L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));

        verify(sectionRepository).findById(15L);
        verifyNoInteractions(textQueryRepository);
    }

    @Test
    void listThrowsForNonAdminWhoIsNotCreator() {
        Section section = buildSectionWithCreatorId(1L);
        when(sectionRepository.findById(5L)).thenReturn(Optional.of(section));

        setAuthentication("carol", "ROLE_USER");
        User user = new User();
        user.setId(99L);
        when(userRepository.findByUsername("carol")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.list(5L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));

        verify(sectionRepository).findById(5L);
        verify(userRepository).findByUsername("carol");
        verifyNoInteractions(textQueryRepository);
    }

    @Test
    void listReturnsResultsForAdmin() {
        Section section = buildSectionWithCreatorId(1L);
        when(sectionRepository.findById(7L)).thenReturn(Optional.of(section));
        List<TextQuery> queries = List.of(new TextQuery(), new TextQuery());
        when(textQueryRepository.findBySection_IdOrderBySequenceAsc(7L)).thenReturn(queries);

        setAuthentication("admin", "ROLE_ADMIN");

        List<TextQuery> result = service.list(7L);

        assertThat(result).isEqualTo(queries);
        verify(textQueryRepository).findBySection_IdOrderBySequenceAsc(7L);
    }

    @Test
    void listReturnsResultsForProjectCreator() {
        Section section = buildSectionWithCreatorId(55L);
        when(sectionRepository.findById(8L)).thenReturn(Optional.of(section));
        List<TextQuery> queries = List.of(new TextQuery());
        when(textQueryRepository.findBySection_IdOrderBySequenceAsc(8L)).thenReturn(queries);

        setAuthentication("dave", "ROLE_USER");
        User creator = new User();
        creator.setId(55L);
        when(userRepository.findByUsername("dave")).thenReturn(Optional.of(creator));

        List<TextQuery> result = service.list(8L);

        assertThat(result).isEqualTo(queries);
        verify(textQueryRepository).findBySection_IdOrderBySequenceAsc(8L);
        verify(userRepository).findByUsername("dave");
    }

    @Test
    void getByIdReturnsEntityWhenPresent() {
        TextQuery query = new TextQuery();
        when(textQueryRepository.findById(10L)).thenReturn(Optional.of(query));

        assertThat(service.getById(10L)).isSameAs(query);
    }

    @Test
    void getByIdThrowsWhenMissing() {
        when(textQueryRepository.findById(11L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(11L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void createPersistsNewQuestionWithSection() {
        TextQueryRequest request = buildRequest();
        request.setSectionId(3L);

        Section section = new Section();
        when(sectionRepository.findById(3L)).thenReturn(Optional.of(section));
        when(textQueryRepository.save(any(TextQuery.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TextQuery result = service.create(request);

        assertThat(result.getAttribute()).isEqualTo("attr");
        assertThat(result.getQuestion()).isEqualTo("Question?");
        assertThat(result.getType()).isEqualTo(1);
        assertThat(result.getSequence()).isEqualTo(42);
        assertThat(result.isRequired()).isTrue();
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.getSection()).isSameAs(section);
        verify(textQueryRepository).save(any(TextQuery.class));
    }

    @Test
    void updateModifiesExistingQuestion() {
        TextQuery existing = new TextQuery();
        when(textQueryRepository.findById(4L)).thenReturn(Optional.of(existing));
        when(textQueryRepository.save(existing)).thenReturn(existing);

        Section section = new Section();
        when(sectionRepository.findById(6L)).thenReturn(Optional.of(section));

        TextQueryRequest request = buildRequest();
        request.setSectionId(6L);

        TextQuery result = service.update(4L, request);

        assertThat(result.getAttribute()).isEqualTo("attr");
        assertThat(result.getQuestion()).isEqualTo("Question?");
        assertThat(result.getSection()).isSameAs(section);
        verify(textQueryRepository).save(existing);
    }

    @Test
    void saveCreatesOptionsForSupportedTypes() {
        TextQueryRequest request = buildRequest();
        request.setId(null);
        request.setType(2);
        request.setSectionId(9L);

        QueryOptionRequest optionRequest = new QueryOptionRequest();
        optionRequest.setSequence(1);
        optionRequest.setDescription("opt");
        optionRequest.setEnabled(true);
        request.setOptions(List.of(optionRequest));

        Section section = new Section();
        when(sectionRepository.findById(9L)).thenReturn(Optional.of(section));
        when(textQueryRepository.save(any(TextQuery.class))).thenAnswer(invocation -> {
            TextQuery saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        service.save(request);

        ArgumentCaptor<QueryOption> optionCaptor = ArgumentCaptor.forClass(QueryOption.class);
        verify(queryOptionRepository).save(optionCaptor.capture());
        QueryOption savedOption = optionCaptor.getValue();
        assertThat(savedOption.getSequence()).isEqualTo(1);
        assertThat(savedOption.getDescription()).isEqualTo("opt");
        assertThat(savedOption.isEnabled()).isTrue();
        assertThat(savedOption.getQuestion()).isNotNull();
    }

    @Test
    void saveSkipsOptionsForUnsupportedType() {
        TextQueryRequest request = buildRequest();
        request.setId(null);
        request.setType(3);
        request.setOptions(List.of(new QueryOptionRequest()));

        when(textQueryRepository.save(any(TextQuery.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.save(request);

        verify(queryOptionRepository, never()).save(any(QueryOption.class));
    }

    @Test
    void deleteDelegatesToRepository() {
        service.delete(123L);

        verify(textQueryRepository).deleteById(123L);
    }

    private Section buildSectionWithCreatorId(Long userId) {
        User creator = new User();
        creator.setId(userId);
        Project project = new Project();
        project.setCreator(creator);
        Section section = new Section();
        section.setProject(project);
        return section;
    }

    private void setAuthentication(String username, String role) {
        var auth = new UsernamePasswordAuthenticationToken(
                username,
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_" + role.replace("ROLE_", "")))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private TextQueryRequest buildRequest() {
        TextQueryRequest request = new TextQueryRequest();
        request.setAttribute("attr");
        request.setQuestion("Question?");
        request.setType(1);
        request.setEnabled(true);
        request.setSequence(42);
        request.setRequired(true);
        return request;
    }
}

