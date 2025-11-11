package io.sci.citizen.service;

import io.sci.citizen.model.User;
import io.sci.citizen.model.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BaseServiceTest {

    @Mock
    private UserRepository userRepository;

    private final TestableBaseService baseService = new TestableBaseService();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void isAdminReturnsTrueWhenRoleAdminPresent() {
        baseService.setUserRepository(userRepository);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "admin",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"))
        );
        setAuthentication(authentication);

        boolean result = baseService.isAdmin();

        assertThat(result).isTrue();
    }

    @Test
    void isAdminReturnsFalseWhenRoleAdminMissing() {
        baseService.setUserRepository(userRepository);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "user",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        setAuthentication(authentication);

        boolean result = baseService.isAdmin();

        assertThat(result).isFalse();
    }

    @Test
    void getUsernameReturnsPrincipalName() {
        baseService.setUserRepository(userRepository);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "carol",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        setAuthentication(authentication);

        String username = baseService.getUsername();

        assertThat(username).isEqualTo("carol");
    }

    @Test
    void getUsernameReturnsNullWhenNoAuthentication() {
        baseService.setUserRepository(userRepository);
        SecurityContextHolder.clearContext();

        String username = baseService.getUsername();

        assertThat(username).isNull();
    }

    @Test
    void getUserWithoutArgumentLooksUpAuthenticatedUser() {
        baseService.setUserRepository(userRepository);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "dave",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        setAuthentication(authentication);
        User expectedUser = new User();
        when(userRepository.findByUsername("dave")).thenReturn(Optional.of(expectedUser));

        User result = baseService.getUser();

        assertThat(result).isSameAs(expectedUser);
        verify(userRepository).findByUsername("dave");
    }

    @Test
    void getUserReturnsNullWhenUsernameNull() {
        baseService.setUserRepository(userRepository);

        User result = baseService.getUser((String) null);

        assertThat(result).isNull();
        verify(userRepository, never()).findByUsername(null);
    }

    @Test
    void getUserReturnsNullWhenUserMissing() {
        baseService.setUserRepository(userRepository);
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        User result = baseService.getUser("unknown");

        assertThat(result).isNull();
    }

    private void setAuthentication(Authentication authentication) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    private static class TestableBaseService extends BaseService {
        void setUserRepository(UserRepository userRepository) {
            this.userRepo = userRepository;
        }
    }
}
