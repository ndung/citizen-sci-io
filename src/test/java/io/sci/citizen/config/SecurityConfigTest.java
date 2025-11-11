package io.sci.citizen.config;

import io.sci.citizen.model.User;
import io.sci.citizen.model.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig();

    @Test
    void userDetailsServiceBuildsUserDetailsWithNormalizedAuthorities() {
        UserRepository repository = mock(UserRepository.class);
        User user = new User();
        user.setUsername("jane");
        user.setPasswordHash("hashed");
        user.setEnabled(true);
        LinkedHashSet<String> roles = new LinkedHashSet<>();
        roles.add("admin");
        roles.add(" ROLE_user");
        roles.add(null);
        roles.add(" ");
        roles.add("Manager");
        user.setRoles(roles);

        when(repository.findByUsername("jane")).thenReturn(Optional.of(user));

        UserDetailsService uds = securityConfig.userDetailsService(repository);
        UserDetails details = uds.loadUserByUsername("jane");

        assertEquals("jane", details.getUsername());
        assertEquals("hashed", details.getPassword());
        assertTrue(details.isAccountNonExpired());
        assertTrue(details.isAccountNonLocked());
        assertTrue(details.isCredentialsNonExpired());
        assertTrue(details.isEnabled());

        Set<String> authorities = details.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        assertEquals(Set.of("ROLE_ADMIN", "ROLE_USER", "ROLE_MANAGER"), authorities);
        verify(repository).findByUsername("jane");
    }

    @Test
    void userDetailsServiceThrowsWhenUserMissing() {
        UserRepository repository = mock(UserRepository.class);
        when(repository.findByUsername("missing")).thenReturn(Optional.empty());

        UserDetailsService uds = securityConfig.userDetailsService(repository);

        assertThrows(UsernameNotFoundException.class, () -> uds.loadUserByUsername("missing"));
        verify(repository).findByUsername("missing");
    }

    @Test
    void requestLoggingFilterIsConfigured() {
        var filter = securityConfig.requestLoggingFilter();

        assertTrue((Boolean) ReflectionTestUtils.invokeMethod(filter, "isIncludeClientInfo"));
        assertTrue((Boolean) ReflectionTestUtils.invokeMethod(filter, "isIncludeQueryString"));
        assertTrue((Boolean) ReflectionTestUtils.invokeMethod(filter, "isIncludeHeaders"));
        assertTrue((Boolean) ReflectionTestUtils.invokeMethod(filter, "isIncludePayload"));
        assertEquals(10_000, (Integer) ReflectionTestUtils.invokeMethod(filter, "getMaxPayloadLength"));
        assertEquals("REQUEST : ", ReflectionTestUtils.getField(filter, "afterMessagePrefix"));
    }

    @Test
    void passwordEncoderReturnsBCryptEncoder() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        assertNotNull(encoder);
        assertTrue(encoder instanceof BCryptPasswordEncoder);
    }

    @Test
    void authProviderUsesSuppliedBeans() {
        UserDetailsService uds = mock(UserDetailsService.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);

        DaoAuthenticationProvider provider = securityConfig.authProvider(uds, encoder);

        assertSame(uds, ReflectionTestUtils.getField(provider, "userDetailsService"));
        Object configuredPasswordEncoder = ReflectionTestUtils.getField(provider, "passwordEncoder");
        assertNotNull(configuredPasswordEncoder);

        if (configuredPasswordEncoder == encoder) {
            assertSame(encoder, configuredPasswordEncoder);
        } else {
            Object delegate = ReflectionTestUtils.getField(configuredPasswordEncoder, "passwordEncoder");
            assertNotNull(delegate);
            assertSame(encoder, delegate);
        }
    }

    @Test
    void authenticationManagerDelegatesToConfiguration() throws Exception {
        AuthenticationConfiguration configuration = mock(AuthenticationConfiguration.class);
        AuthenticationManager manager = mock(AuthenticationManager.class);
        when(configuration.getAuthenticationManager()).thenReturn(manager);

        AuthenticationManager result = securityConfig.authenticationManager(configuration);

        assertSame(manager, result);
        verify(configuration).getAuthenticationManager();
    }
}
