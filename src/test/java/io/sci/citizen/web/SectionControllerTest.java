package io.sci.citizen.web;

import io.sci.citizen.model.Project;
import io.sci.citizen.model.Section;
import io.sci.citizen.model.User;
import io.sci.citizen.model.dto.SectionRequest;
import io.sci.citizen.service.ProjectService;
import io.sci.citizen.service.SectionService;
import io.sci.citizen.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class SectionControllerTest {

    @Mock
    private SectionService sectionService;

    @Mock
    private ProjectService projectService;

    @Mock
    private UserService userService;

    @InjectMocks
    private SectionController controller;

    private MockMvc mockMvc;

    private AutoCloseable closeable;

    private User currentUser;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setValidator(validator)
                .build();

        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("jane");

        when(userService.getUser("jane")).thenReturn(currentUser);

        var auth = new UsernamePasswordAuthenticationToken(
                "jane",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() throws Exception {
        SecurityContextHolder.clearContext();
        closeable.close();
    }

    @Test
    void listPopulatesModelWhenAuthorized() throws Exception {
        Project project = projectOwnedByCurrentUser(5L);
        Section entity = new Section();
        entity.setId(20L);
        entity.setName("Water Quality");
        entity.setType("survey");
        entity.setSequence(3);
        entity.setEnabled(true);
        entity.setProject(project);

        when(projectService.getById(5L)).thenReturn(project);
        when(sectionService.getById(20L)).thenReturn(entity);
        when(sectionService.list(Optional.of(5L))).thenReturn(List.of(entity));

        mockMvc.perform(get("/sections")
                        .param("projectId", "5")
                        .param("sectionId", "20"))
                .andExpect(status().isOk())
                .andExpect(view().name("sections"))
                .andExpect(model().attributeExists("sections", "section", "projects", "typeOptions"))
                .andExpect(model().attribute("section", allOf(
                        hasProperty("id", equalTo(20L)),
                        hasProperty("name", equalTo("Water Quality")),
                        hasProperty("type", equalTo("survey")),
                        hasProperty("sequence", equalTo(3)),
                        hasProperty("enabled", equalTo(true)),
                        hasProperty("projectId", equalTo(5L))
                )));

        verify(sectionService).list(Optional.of(5L));
        verify(sectionService).getById(20L);
    }

    @Test
    void listRejectsUnauthorizedProject() throws Exception {
        Project project = projectOwnedByAnotherUser(8L);
        when(projectService.getById(8L)).thenReturn(project);

        mockMvc.perform(get("/sections").param("projectId", "8"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createReturnsFormWhenValidationFails() throws Exception {
        Project project = projectOwnedByCurrentUser(11L);
        when(projectService.getById(11L)).thenReturn(project);
        when(sectionService.list(Optional.of(11L))).thenReturn(List.of());

        mockMvc.perform(post("/sections")
                        .param("projectId", "11")
                        .param("sequence", "1")
                        .param("type", "")
                        .param("name", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("sections"));

        verify(sectionService, never()).create(any(SectionRequest.class));
    }

    @Test
    void createPersistsSectionWhenValid() throws Exception {
        Project project = projectOwnedByCurrentUser(15L);
        when(projectService.getById(15L)).thenReturn(project);
        when(sectionService.list(Optional.of(15L))).thenReturn(List.of());

        mockMvc.perform(post("/sections")
                        .param("projectId", "15")
                        .param("sequence", "2")
                        .param("type", "survey")
                        .param("name", "Habitat")
                        .param("enabled", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sections?projectId=15"))
                .andExpect(flash().attribute("sectionSaved", true));

        ArgumentCaptor<SectionRequest> requestCaptor = ArgumentCaptor.forClass(SectionRequest.class);
        verify(sectionService).create(requestCaptor.capture());
        SectionRequest captured = requestCaptor.getValue();
        assertThat(captured.getProjectId(), equalTo(15L));
        assertThat(captured.getName(), equalTo("Habitat"));
        assertThat(captured.getType(), equalTo("survey"));
        assertThat(captured.getSequence(), equalTo(2));
        assertThat(captured.isEnabled(), equalTo(true));
    }

    @Test
    void createRejectsUnauthorizedProject() throws Exception {
        Project project = projectOwnedByAnotherUser(22L);
        when(projectService.getById(22L)).thenReturn(project);

        mockMvc.perform(post("/sections")
                        .param("projectId", "22")
                        .param("sequence", "1")
                        .param("type", "survey")
                        .param("name", "Quality"))
                .andExpect(status().isForbidden());

        verify(sectionService, never()).create(any(SectionRequest.class));
    }

    @Test
    void deleteRejectsUnauthorizedAccess() throws Exception {
        Project project = projectOwnedByAnotherUser(30L);
        Section section = new Section();
        section.setId(301L);
        section.setProject(project);

        when(sectionService.getById(301L)).thenReturn(section);

        mockMvc.perform(post("/sections/301/delete"))
                .andExpect(status().isForbidden());

        verify(sectionService, never()).delete(301L);
    }

    @Test
    void deleteRemovesSectionWhenAuthorized() throws Exception {
        Project project = projectOwnedByCurrentUser(33L);
        Section section = new Section();
        section.setId(332L);
        section.setProject(project);

        when(sectionService.getById(332L)).thenReturn(section);

        mockMvc.perform(post("/sections/332/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sections?projectId=33"))
                .andExpect(flash().attribute("success", "Section deleted."));

        verify(sectionService).delete(332L);
    }

    private Project projectOwnedByCurrentUser(Long id) {
        Project project = new Project();
        project.setId(id);
        project.setCreator(currentUser);
        return project;
    }

    private Project projectOwnedByAnotherUser(Long id) {
        User other = new User();
        other.setId(99L);
        other.setUsername("other");

        Project project = new Project();
        project.setId(id);
        project.setCreator(other);
        return project;
    }
}
