package io.sci.citizen.api;

import io.sci.citizen.api.component.JwtTokenUtil;
import io.sci.citizen.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BaseApiControllerTest {

    private BaseApiController controller;

    @Mock
    private JwtTokenUtil tokenUtil;

    @BeforeEach
    void setUp() {
        controller = new BaseApiController();
        ReflectionTestUtils.setField(controller, "tokenUtil", tokenUtil);
    }

    @Test
    void forbiddenConstantHasForbiddenStatus() {
        assertEquals(HttpStatus.FORBIDDEN, BaseApiController.FORBIDDEN.getStatusCode());
    }

    @Test
    void createTokenDelegatesToTokenUtil() {
        User user = new User();
        String expectedToken = "token";
        when(tokenUtil.createToken(user)).thenReturn(expectedToken);

        String actual = controller.createToken(user);

        assertEquals(expectedToken, actual);
    }

    @Test
    void authorizeReturnsTrueWhenRolesPresent() {
        String token = "token";
        when(tokenUtil.getRoles(token)).thenReturn("ROLE_USER");

        assertTrue(controller.authorize(token));
    }

    @Test
    void authorizeReturnsFalseWhenRolesMissing() {
        String token = "token";
        when(tokenUtil.getRoles(token)).thenReturn(null);

        assertFalse(controller.authorize(token));
    }

    @Test
    void authorizeReturnsFalseWhenTokenUtilThrows() {
        String token = "token";
        when(tokenUtil.getRoles(token)).thenThrow(new RuntimeException("boom"));

        assertFalse(controller.authorize(token));
    }

    @Test
    void authenticateDelegatesToTokenUtil() {
        String token = "token";
        when(tokenUtil.authenticate(token)).thenReturn(true);

        assertTrue(controller.authenticate(token));
    }

    @Test
    void getUserIdDelegatesToTokenUtil() {
        String token = "token";
        when(tokenUtil.getUserId(token)).thenReturn("123");

        assertEquals("123", controller.getUserId(token));
    }

    @Test
    void getHttpStatusReturnsBadRequestWhenDataMissing() {
        Response response = new Response();

        ResponseEntity<Response> entity = controller.getHttpStatus(response);

        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
        assertSame(response, entity.getBody());
    }

    @Test
    void getHttpStatusReturnsOkWhenDataPresent() {
        Response response = new Response();
        response.setData("data");

        ResponseEntity<Response> entity = controller.getHttpStatus(response);

        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertSame(response, entity.getBody());
    }

    @Test
    void responseReturnsResponseEntityWithStatus() {
        Response response = new Response("data");

        ResponseEntity<Response> entity = controller.response(HttpStatus.CREATED, response);

        assertEquals(HttpStatus.CREATED, entity.getStatusCode());
        assertSame(response, entity.getBody());
    }

    @Test
    void getHttpStatusWithHeadersUsesProvidedHeaders() {
        Response response = new Response();
        response.setData("data");
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Test", "value");

        ResponseEntity<Response> entity = controller.getHttpStatus(response, headers);

        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertSame(response, entity.getBody());
        assertEquals("value", entity.getHeaders().getFirst("X-Test"));
    }
}
