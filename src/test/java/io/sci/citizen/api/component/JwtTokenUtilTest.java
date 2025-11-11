package io.sci.citizen.api.component;

import io.sci.citizen.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenUtilTest {

    private static final String RAW_SECRET = "0123456789ABCDEF0123456789ABCDEF";
    private static final String SECRET = Base64.getEncoder()
            .encodeToString(RAW_SECRET.getBytes(StandardCharsets.UTF_8));

    private JwtTokenUtil jwtTokenUtil;

    @BeforeEach
    void setUp() {
        jwtTokenUtil = new JwtTokenUtil();
        ReflectionTestUtils.setField(jwtTokenUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtTokenUtil, "expiration", 60L);
    }

    @Test
    void createTokenIncludesExpectedClaims() {
        User user = buildUser();

        String token = jwtTokenUtil.createToken(user);

        assertNotNull(token);
        assertEquals(user.getUsername(), jwtTokenUtil.getUsername(token));
        assertEquals(String.join(", ", user.getRoles()), jwtTokenUtil.getRoles(token));
        assertEquals(String.valueOf(user.getId()), jwtTokenUtil.getUserId(token));
    }

    @Test
    void authenticateReturnsTrueForValidToken() {
        String token = jwtTokenUtil.createToken(buildUser());

        assertTrue(jwtTokenUtil.authenticate(token));
    }

    @Test
    void authenticateReturnsFalseForInvalidToken() {
        assertFalse(jwtTokenUtil.authenticate("invalid.token.value"));
    }

    @Test
    void isValidReturnsTrueForMatchingUserDetails() {
        User user = buildUser();
        String token = jwtTokenUtil.createToken(user);
        UserDetails userDetails = buildUserDetails(user.getUsername());

        assertTrue(jwtTokenUtil.isValid(token, userDetails));
    }

    @Test
    void isValidReturnsFalseWhenUsernameDoesNotMatch() {
        String token = jwtTokenUtil.createToken(buildUser());
        UserDetails userDetails = buildUserDetails("other-user");

        assertFalse(jwtTokenUtil.isValid(token, userDetails));
    }

    @Test
    void isValidReturnsFalseWhenTokenExpired() throws InterruptedException {
        ReflectionTestUtils.setField(jwtTokenUtil, "expiration", 0L);
        String token = jwtTokenUtil.createToken(buildUser());
        Thread.sleep(5);
        UserDetails userDetails = buildUserDetails("test-user");

        assertFalse(jwtTokenUtil.isValid(token, userDetails));
    }

    private User buildUser() {
        User user = new User();
        user.setId(42L);
        user.setUsername("test-user");
        user.setRoles(new LinkedHashSet<>(List.of("ROLE_USER", "ROLE_ADMIN")));
        return user;
    }

    private UserDetails buildUserDetails(String username) {
        return org.springframework.security.core.userdetails.User
                .withUsername(username)
                .password("password")
                .authorities("ROLE_USER")
                .build();
    }
}
