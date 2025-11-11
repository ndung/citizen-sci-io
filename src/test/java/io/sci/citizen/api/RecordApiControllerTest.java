package io.sci.citizen.api;

import io.sci.citizen.api.dto.DataRequest;
import io.sci.citizen.api.dto.SummaryResponse;
import io.sci.citizen.model.Data;
import io.sci.citizen.service.RecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordApiControllerTest {

    @Mock
    private RecordService recordService;

    private RecordApiController controller;

    @BeforeEach
    void setUp() {
        controller = spy(new RecordApiController(recordService));
    }

    @Test
    void uploadReturnsForbiddenWhenAuthorizationFails() {
        String token = "token";
        doReturn(false).when(controller).authorize(token);

        ResponseEntity<Response> response = controller.upload(token, "model", null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(recordService, never()).record(anyLong(), anyString(), any(), anyString());
    }

    @Test
    void uploadDelegatesToRecordServiceWhenAuthorized() throws Exception {
        String token = "token";
        MultipartFile image = mock(MultipartFile.class);
        MultipartFile[] images = new MultipartFile[]{image};
        doReturn(true).when(controller).authorize(token);
        doReturn("5").when(controller).getUserId(token);

        ResponseEntity<Response> response = controller.upload(token, "model-json", images, "results");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(Boolean.TRUE);
        verify(recordService).record(5L, "model-json", images, "results");
    }

    @Test
    void uploadReturnsBadRequestWhenServiceThrows() throws Exception {
        String token = "token";
        doReturn(true).when(controller).authorize(token);
        doReturn("3").when(controller).getUserId(token);
        doThrow(new IOException("boom")).when(recordService).record(anyLong(), anyString(), any(), anyString());

        ResponseEntity<Response> response = controller.upload(token, "model", null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("boom");
    }

    @Test
    void getListByProjectReturnsForbiddenWhenAuthorizationFails() {
        String token = "token";
        DataRequest request = new DataRequest(42L, 1);
        doReturn(false).when(controller).authorize(token);

        ResponseEntity<Response> response = controller.getListByProject(token, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(recordService, never()).getRecordsByUserAndProject(anyLong(), anyLong(), any());
    }

    @Test
    void getListByProjectReturnsRecordsWhenAuthorized() {
        String token = "token";
        DataRequest request = new DataRequest(7L, 2);
        List<Data> records = List.of(new Data());
        doReturn(true).when(controller).authorize(token);
        doReturn("11").when(controller).getUserId(token);
        when(recordService.getRecordsByUserAndProject(11L, 7L, 2)).thenReturn(records);

        ResponseEntity<Response> response = controller.getListByProject(token, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isSameAs(records);
        verify(recordService).getRecordsByUserAndProject(11L, 7L, 2);
    }

    @Test
    void getListByUserReturnsForbiddenWhenAuthorizationFails() {
        String token = "token";
        DataRequest request = new DataRequest(null, 0);
        doReturn(false).when(controller).authorize(token);

        ResponseEntity<Response> response = controller.getListByUser(token, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(recordService, never()).getRecordsByUser(anyLong(), any());
    }

    @Test
    void getListByUserReturnsRecordsWhenAuthorized() {
        String token = "token";
        DataRequest request = new DataRequest(null, 1);
        List<Data> records = List.of(new Data(), new Data());
        doReturn(true).when(controller).authorize(token);
        doReturn("9").when(controller).getUserId(token);
        when(recordService.getRecordsByUser(9L, 1)).thenReturn(records);

        ResponseEntity<Response> response = controller.getListByUser(token, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isSameAs(records);
        verify(recordService).getRecordsByUser(9L, 1);
    }

    @Test
    void getProjectSummaryReturnsForbiddenWhenAuthorizationFails() {
        String token = "token";
        DataRequest request = new DataRequest(21L, null);
        doReturn(false).when(controller).authorize(token);

        ResponseEntity<Response> response = controller.getProjectSummary(token, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(recordService, never()).getRecordsSummaryByUserAndProject(anyLong(), anyLong());
    }

    @Test
    void getProjectSummaryReturnsSummaryWhenAuthorized() {
        String token = "token";
        DataRequest request = new DataRequest(4L, null);
        int[] summaryArray = new int[]{1, 2, 3};
        doReturn(true).when(controller).authorize(token);
        doReturn("8").when(controller).getUserId(token);
        when(recordService.getRecordsSummaryByUserAndProject(8L, 4L)).thenReturn(summaryArray);

        ResponseEntity<Response> response = controller.getProjectSummary(token, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        Object data = response.getBody().getData();
        assertThat(data).isInstanceOf(SummaryResponse.class);
        SummaryResponse summary = (SummaryResponse) data;
        assertThat(summary.uploaded()).isEqualTo(1);
        assertThat(summary.verified()).isEqualTo(2);
        assertThat(summary.total()).isEqualTo(3);
        verify(recordService).getRecordsSummaryByUserAndProject(8L, 4L);
    }

    @Test
    void getUserSummaryReturnsForbiddenWhenAuthorizationFails() {
        String token = "token";
        doReturn(false).when(controller).authorize(token);

        ResponseEntity<Response> response = controller.getUserSummary(token);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(recordService, never()).getRecordsSummaryByUser(anyLong());
    }

    @Test
    void getUserSummaryReturnsSummaryWhenAuthorized() {
        String token = "token";
        int[] summaryArray = new int[]{5, 6, 7};
        doReturn(true).when(controller).authorize(token);
        doReturn("12").when(controller).getUserId(token);
        when(recordService.getRecordsSummaryByUser(12L)).thenReturn(summaryArray);

        ResponseEntity<Response> response = controller.getUserSummary(token);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        Object data = response.getBody().getData();
        assertThat(data).isInstanceOf(SummaryResponse.class);
        SummaryResponse summary = (SummaryResponse) data;
        assertThat(summary.uploaded()).isEqualTo(5);
        assertThat(summary.verified()).isEqualTo(6);
        assertThat(summary.total()).isEqualTo(7);
        verify(recordService).getRecordsSummaryByUser(12L);
    }
}
