package io.sci.citizen.web;

import io.sci.citizen.model.Data;
import io.sci.citizen.service.DataService;
import io.sci.citizen.service.RecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordControllerTest {

    @Mock
    private DataService dataService;

    @Mock
    private RecordService recordService;

    @InjectMocks
    private RecordController recordController;

    private Model model;

    @BeforeEach
    void setUp() {
        model = mock(Model.class);
    }

    @Test
    void listAddsRecordToModelAndReturnsView() {
        Data data = new Data();
        when(dataService.getById(3L)).thenReturn(data);

        String viewName = recordController.list(3L, model);

        assertThat(viewName).isEqualTo("records");
        verify(dataService).getById(3L);
        verify(model).addAttribute("record", data);
    }

    @Test
    void approveDataUpdatesStatusAndRedirects() {
        String viewName = recordController.approveData(7L);

        assertThat(viewName).isEqualTo("redirect:/record/7");
        verify(dataService).updateStatus(7L, 1);
    }

    @Test
    void rejectDataUpdatesStatusAndRedirects() {
        String viewName = recordController.rejectData(8L);

        assertThat(viewName).isEqualTo("redirect:/record/8");
        verify(dataService).updateStatus(8L, 2);
    }

    @Test
    void approveImageUpdatesStatusAndRedirects() {
        String viewName = recordController.approveImage(5L, 9L);

        assertThat(viewName).isEqualTo("redirect:/record/5");
        verify(recordService).updateStatus(9L, 1);
    }

    @Test
    void rejectImageUpdatesStatusAndRedirects() {
        String viewName = recordController.rejectImage(4L, 6L);

        assertThat(viewName).isEqualTo("redirect:/record/4");
        verify(recordService).updateStatus(6L, 2);
    }
}
