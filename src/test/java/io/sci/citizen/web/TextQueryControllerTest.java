package io.sci.citizen.web;

import io.sci.citizen.model.Project;
import io.sci.citizen.model.QueryOption;
import io.sci.citizen.model.Section;
import io.sci.citizen.model.TextQuery;
import io.sci.citizen.model.dto.TextQueryRequest;
import io.sci.citizen.service.SectionService;
import io.sci.citizen.service.TextQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TextQueryControllerTest {

    @Mock
    private TextQueryService textQueryService;

    @Mock
    private SectionService sectionService;

    private TextQueryController controller;

    private Model model;

    @BeforeEach
    void setUp() {
        controller = spy(new TextQueryController(textQueryService, sectionService));
        model = new ExtendedModelMap();
    }

    @Test
    void listWithoutQueryIdPopulatesModelAndReturnsView() {
        Long sectionId = 12L;
        List<TextQuery> queries = List.of(new TextQuery());
        Section section = new Section();
        section.setId(sectionId);

        when(textQueryService.list(sectionId)).thenReturn(queries);
        when(sectionService.getById(sectionId)).thenReturn(section);

        String view = controller.list(null, sectionId, model);

        assertThat(view).isEqualTo("questions");
        assertThat(model.getAttribute("queries")).isEqualTo(queries);
        TextQueryRequest form = (TextQueryRequest) model.getAttribute("query");
        assertThat(form).isNotNull();
        assertThat(form.getOptions()).hasSize(1);
        assertThat(model.getAttribute("sections")).isEqualTo(List.of(section));
        @SuppressWarnings("unchecked")
        Map<Integer, String> typeOptions = (Map<Integer, String>) model.getAttribute("typeOptions");
        assertThat(typeOptions)
                .containsEntry(2, "Radio buttons")
                .containsEntry(3, "String text field")
                .containsEntry(8, "Free decimal input");
    }

    @Test
    void listWithQueryIdPopulatesExistingQueryIntoModel() {
        Long queryId = 5L;
        Long sectionId = 9L;
        TextQuery entity = new TextQuery();
        entity.setId(queryId);
        entity.setSequence(4);
        entity.setType(2);
        entity.setQuestion("Favorite color?");
        entity.setEnabled(true);
        entity.setAttribute("color");
        entity.setRequired(true);

        Project project = new Project();
        Section section = new Section();
        section.setId(sectionId);
        section.setProject(project);
        entity.setSection(section);

        QueryOption option1 = new QueryOption();
        option1.setId(11L);
        option1.setSequence(1);
        option1.setDescription("Red");
        option1.setEnabled(true);
        QueryOption option2 = new QueryOption();
        option2.setId(12L);
        option2.setSequence(2);
        option2.setDescription("Blue");
        option2.setEnabled(false);
        entity.setOptions(List.of(option1, option2));

        when(textQueryService.getById(queryId)).thenReturn(entity);
        when(textQueryService.list(sectionId)).thenReturn(List.of(entity));
        when(sectionService.getById(sectionId)).thenReturn(section);
        doReturn(true).when(controller).isAuthorized(project);

        String view = controller.list(queryId, sectionId, model);

        assertThat(view).isEqualTo("questions");
        TextQueryRequest form = (TextQueryRequest) model.getAttribute("query");
        assertThat(form.getId()).isEqualTo(queryId);
        assertThat(form.getSectionId()).isEqualTo(sectionId);
        assertThat(form.getSequence()).isEqualTo(4);
        assertThat(form.getType()).isEqualTo(2);
        assertThat(form.getQuestion()).isEqualTo("Favorite color?");
        assertThat(form.getAttribute()).isEqualTo("color");
        assertThat(form.isEnabled()).isTrue();
        assertThat(form.isRequired()).isTrue();
        assertThat(form.getOptions()).hasSize(2);
        assertThat(form.getOptions().get(0).getId()).isEqualTo(11L);
        assertThat(form.getOptions().get(0).getDescription()).isEqualTo("Red");
        assertThat(form.getOptions().get(1).getId()).isEqualTo(12L);
        assertThat(form.getOptions().get(1).getDescription()).isEqualTo("Blue");
        assertThat(model.getAttribute("queries")).isEqualTo(List.of(entity));
    }

    @Test
    void listWithUnauthorizedQueryThrowsForbidden() {
        Long queryId = 2L;
        Project project = new Project();
        Section section = new Section();
        section.setProject(project);
        TextQuery entity = new TextQuery();
        entity.setSection(section);

        when(textQueryService.getById(queryId)).thenReturn(entity);
        doReturn(false).when(controller).isAuthorized(project);

        assertThatThrownBy(() -> controller.list(queryId, 3L, model))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void createWithBindingErrorsReturnsQuestionsView() {
        TextQueryRequest form = new TextQueryRequest();
        form.setSectionId(7L);
        BindingResult binding = mock(BindingResult.class);
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        Section section = new Section();
        section.setId(7L);

        when(binding.hasErrors()).thenReturn(true);
        when(textQueryService.list(7L)).thenReturn(List.of());
        when(sectionService.getById(7L)).thenReturn(section);

        String view = controller.create(form, binding, redirectAttributes, model);

        assertThat(view).isEqualTo("questions");
        assertThat(model.getAttribute("query")).isSameAs(form);
        verify(textQueryService, never()).save(any());
    }

    @Test
    void createWithValidFormSavesQueryAndRedirects() {
        TextQueryRequest form = new TextQueryRequest();
        form.setSectionId(10L);
        BindingResult binding = mock(BindingResult.class);
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        when(binding.hasErrors()).thenReturn(false);

        String view = controller.create(form, binding, redirectAttributes, model);

        assertThat(view).isEqualTo("redirect:/questions?sectionId=10");
        verify(textQueryService).save(form);
        assertThat(redirectAttributes.getFlashAttributes()).containsEntry("querySaved", true);
    }

    @Test
    void editFormRedirectsToListWhenAuthorized() {
        Long queryId = 6L;
        Project project = new Project();
        Section section = new Section();
        section.setId(4L);
        section.setProject(project);
        TextQuery entity = new TextQuery();
        entity.setId(queryId);
        entity.setSection(section);

        when(textQueryService.getById(queryId)).thenReturn(entity);
        doReturn(true).when(controller).isAuthorized(project);

        String view = controller.editForm(queryId);

        assertThat(view).isEqualTo("redirect:/questions?sectionId=4&queryId=6");
    }

    @Test
    void editFormThrowsForbiddenWhenNotAuthorized() {
        Long queryId = 6L;
        Project project = new Project();
        Section section = new Section();
        section.setProject(project);
        TextQuery entity = new TextQuery();
        entity.setSection(section);

        when(textQueryService.getById(queryId)).thenReturn(entity);
        doReturn(false).when(controller).isAuthorized(project);

        assertThatThrownBy(() -> controller.editForm(queryId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void updateWithBindingErrorsReturnsQuestionsView() {
        TextQueryRequest form = new TextQueryRequest();
        form.setSectionId(5L);
        BindingResult binding = mock(BindingResult.class);
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        Section section = new Section();
        section.setId(5L);

        when(binding.hasErrors()).thenReturn(true);
        when(textQueryService.list(5L)).thenReturn(List.of());
        when(sectionService.getById(5L)).thenReturn(section);

        String view = controller.update(3L, form, binding, redirectAttributes, model);

        assertThat(view).isEqualTo("questions");
        assertThat(model.getAttribute("query")).isSameAs(form);
        verify(textQueryService, never()).save(any());
    }

    @Test
    void updateWithValidFormSavesQueryAndRedirects() {
        TextQueryRequest form = new TextQueryRequest();
        form.setSectionId(14L);
        BindingResult binding = mock(BindingResult.class);
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        when(binding.hasErrors()).thenReturn(false);

        String view = controller.update(2L, form, binding, redirectAttributes, model);

        assertThat(view).isEqualTo("redirect:/questions?sectionId=14");
        verify(textQueryService).save(form);
        assertThat(redirectAttributes.getFlashAttributes()).containsEntry("querySaved", true);
    }
}
