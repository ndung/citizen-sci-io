package io.sci.citizen.api.component;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import java.io.IOException;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldLeaveContextUnauthenticatedWhenAuthorizationHeaderMissing() throws ServletException, IOException {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtTokenUtil, userDetailsService);
    }

    @Test
    void shouldPopulateSecurityContextWhenTokenIsValid() throws ServletException, IOException {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "token");
        var response = new MockHttpServletResponse();

        UserDetails user = User.withUsername("jane")
                .password("password")
                .roles("USER")
                .build();

        when(jwtTokenUtil.getUsername("token")).thenReturn("jane");
        when(userDetailsService.loadUserByUsername("jane")).thenReturn(user);
        when(jwtTokenUtil.isValid("token", user)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(user);
        assertThat(authentication.getCredentials()).isNull();
        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyElementsOf(
                        user.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.toList())
                );
        assertThat(authentication.getDetails()).isInstanceOf(WebAuthenticationDetails.class);

        verify(jwtTokenUtil).getUsername("token");
        verify(userDetailsService).loadUserByUsername("jane");
        verify(jwtTokenUtil).isValid("token", user);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWhenTokenIsInvalid() throws ServletException, IOException {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "token");
        var response = new MockHttpServletResponse();

        when(jwtTokenUtil.getUsername("token")).thenReturn("john");
        var user = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("john")).thenReturn(user);
        when(jwtTokenUtil.isValid("token", user)).thenReturn(false);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verify(jwtTokenUtil).getUsername("token");
        verify(userDetailsService).loadUserByUsername("john");
        verify(jwtTokenUtil).isValid("token", user);
        verify(filterChain).doFilter(request, response);
    }
}
