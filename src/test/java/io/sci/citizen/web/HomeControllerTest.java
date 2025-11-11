package io.sci.citizen.web;

import io.sci.citizen.model.Project;
import io.sci.citizen.model.dto.ProjectData;
import io.sci.citizen.service.DataService;
import io.sci.citizen.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    @Mock
    private ProjectService projectService;

    @Mock
    private DataService dataService;

    @InjectMocks
    private HomeController homeController;

    @Test
    void rootRedirectsToDashboard() {
        String view = homeController.root();

        assertThat(view).isEqualTo("redirect:/dashboard");
    }

    @Test
    void loginReturnsLoginView() {
        String view = homeController.login();

        assertThat(view).isEqualTo("login");
    }

    @Test
    void dashboardPopulatesProjectDataAndReturnsView() {
        Project projectOne = new Project();
        projectOne.setId(1L);
        projectOne.setCreatedAt(new Date(1704067200000L)); // 01/01/2024 00:00 UTC

        Project projectTwo = new Project();
        projectTwo.setId(2L);
        projectTwo.setCreatedAt(new Date(1711929600000L)); // 01/04/2024 00:00 UTC

        when(projectService.findAll()).thenReturn(List.of(projectOne, projectTwo));
        when(dataService.getProjectSummary(1L)).thenReturn(List.of(1, 1));
        when(dataService.getProjectSummary(2L)).thenReturn(List.of(3, 5));

        Model model = new ConcurrentModel();

        String view = homeController.dashboard(model);

        assertThat(view).isEqualTo("dashboard");
        assertThat(model.containsAttribute("projects")).isTrue();

        @SuppressWarnings("unchecked")
        List<ProjectData> projectDataList = (List<ProjectData>) model.getAttribute("projects");
        assertThat(projectDataList).hasSize(2);
        assertThat(projectDataList.get(0).project()).isSameAs(projectOne);
        assertThat(projectDataList.get(1).project()).isSameAs(projectTwo);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String expectedDescriptionOne = "Project was created at " + sdf.format(projectOne.getCreatedAt())
                + ". Contributors: 1 user, data: 1 record.";
        String expectedDescriptionTwo = "Project was created at " + sdf.format(projectTwo.getCreatedAt())
                + ". Contributors: 3 users, data: 5 records.";

        assertThat(projectDataList.get(0).description()).isEqualTo(expectedDescriptionOne);
        assertThat(projectDataList.get(1).description()).isEqualTo(expectedDescriptionTwo);

        verify(projectService, times(1)).findAll();
        verify(dataService, times(2)).getProjectSummary(anyLong());
    }
}
