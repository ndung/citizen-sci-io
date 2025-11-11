package io.sci.citizen.web;

import io.sci.citizen.model.Data;
import io.sci.citizen.service.DataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataControllerTest {

    @Mock
    private DataService dataService;

    @InjectMocks
    private DataController controller;

    private Model model;

    @BeforeEach
    void setUp() {
        model = new ExtendedModelMap();
    }

    @Test
    void listWithoutProjectIdAddsAllDataAndReturnsView() {
        List<Data> data = List.of(new Data(), new Data());
        when(dataService.findAll()).thenReturn(data);

        String viewName = controller.list(model);

        assertThat(viewName).isEqualTo("data");
        assertThat(model.getAttribute("data")).isEqualTo(data);
        verify(dataService).findAll();
    }

    @Test
    void listWithProjectIdUsesServiceFilterAndReturnsView() {
        List<Data> projectData = List.of(new Data());
        when(dataService.findAll(7L)).thenReturn(projectData);

        String viewName = controller.list(7L, model);

        assertThat(viewName).isEqualTo("data");
        assertThat(model.getAttribute("data")).isEqualTo(projectData);
        verify(dataService).findAll(7L);
        verify(dataService, never()).findAll();
    }
}
