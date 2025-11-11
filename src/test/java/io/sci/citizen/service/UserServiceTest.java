package io.sci.citizen.service;

import io.sci.citizen.model.User;
import io.sci.citizen.model.dto.CreatePasswordRequest;
import io.sci.citizen.model.dto.UserRequest;
import io.sci.citizen.model.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void findAllReturnsUsersSortedByIdDescending() {
        var users = List.of(new User(), new User());
        when(userRepository.findAll(Sort.by("id").descending())).thenReturn(users);

        var result = userService.findAll();

        assertThat(result).isEqualTo(users);
        verify(userRepository).findAll(Sort.by("id").descending());
    }

    @Test
    void getByIdReturnsUserWhenPresent() {
        var user = new User();
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));

        var result = userService.getById(42L);

        assertThat(result).isSameAs(user);
    }

    @Test
    void getByIdThrowsWhenUserMissing() {
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(10L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void isUserNameExistedDelegatesToRepository() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThat(userService.isUserNameExisted("alice")).isTrue();
    }

    @Test
    void isEmailExistedDelegatesToRepository() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);

        assertThat(userService.isEmailExisted("alice@example.com")).isFalse();
    }

    @Test
    void createPersistsTrimmedUserWithEncodedPassword() {
        UserRequest request = new UserRequest();
        request.setUsername(" alice ");
        request.setFullName(" Alice Anderson ");
        request.setEmail(" alice@example.com ");
        request.setEnabled(true);
        request.setRolesCsv("ADMIN, user");
        request.setPassword("secret");

        when(passwordEncoder.encode("secret")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.create(request);

        assertThat(result.getUsername()).isEqualTo("alice");
        assertThat(result.getFullName()).isEqualTo("Alice Anderson");
        assertThat(result.getEmail()).isEqualTo("alice@example.com");
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.getRoles()).containsExactlyInAnyOrder("ADMIN", "USER");
        assertThat(result.getPasswordHash()).isEqualTo("encoded");
        verify(passwordEncoder).encode("secret");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void signUpCreatesDisabledUserWithDefaultRole() {
        UserRequest request = new UserRequest();
        request.setUsername("bob");
        request.setFullName("Bob Brown");
        request.setEmail("bob@example.com");
        request.setPassword("pass123");

        when(passwordEncoder.encode("pass123")).thenReturn("encoded-pass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.signUp(request);

        assertThat(result.getUsername()).isEqualTo("bob");
        assertThat(result.getFullName()).isEqualTo("Bob Brown");
        assertThat(result.getEmail()).isEqualTo("bob@example.com");
        assertThat(result.isEnabled()).isFalse();
        assertThat(result.getRoles()).containsExactly("USER");
        assertThat(result.getPasswordHash()).isEqualTo("encoded-pass");
    }

    @Test
    void updateReplacesUserFieldsButNotPasswordWhenBlank() {
        User existing = new User();
        existing.setPasswordHash("old-hash");
        when(userRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        UserRequest request = new UserRequest();
        request.setUsername(" charlie ");
        request.setFullName(" Charlie Clark ");
        request.setEmail(" charlie@example.com ");
        request.setEnabled(false);
        request.setRolesCsv("user");
        request.setPassword(" ");

        User result = userService.update(7L, request);

        assertThat(result.getUsername()).isEqualTo("charlie");
        assertThat(result.getFullName()).isEqualTo("Charlie Clark");
        assertThat(result.getEmail()).isEqualTo("charlie@example.com");
        assertThat(result.isEnabled()).isFalse();
        assertThat(result.getRoles()).containsExactly("USER");
        assertThat(result.getPasswordHash()).isEqualTo("old-hash");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void updateEncodesPasswordWhenProvided() {
        User existing = new User();
        existing.setPasswordHash("old");
        when(userRepository.findById(9L)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);
        when(passwordEncoder.encode("new-pass")).thenReturn("new-hash");

        UserRequest request = new UserRequest();
        request.setUsername("dan");
        request.setFullName("Dan Doe");
        request.setEmail("dan@example.com");
        request.setEnabled(true);
        request.setRolesCsv("ADMIN");
        request.setPassword("new-pass");

        User result = userService.update(9L, request);

        assertThat(result.getPasswordHash()).isEqualTo("new-hash");
        verify(passwordEncoder).encode("new-pass");
    }

    @Test
    void updateProfileUsingAuthenticatedUserDelegatesToRepository() {
        User authenticated = new User();
        authenticated.setUsername("eve");
        when(userRepository.findByUsername("eve")).thenReturn(Optional.of(authenticated));
        when(userRepository.save(authenticated)).thenReturn(authenticated);

        var auth = new UsernamePasswordAuthenticationToken(
                "eve",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        UserRequest request = new UserRequest();
        request.setUsername(" eve-new ");
        request.setFullName(" Eve Example ");
        request.setEmail(" eve@example.com ");

        User result = userService.updateProfile(request);

        assertThat(result.getUsername()).isEqualTo("eve-new");
        assertThat(result.getFullName()).isEqualTo("Eve Example");
        assertThat(result.getEmail()).isEqualTo("eve@example.com");
    }

    @Test
    void changePasswordWithAuthenticatedUserValidatesAndSaves() {
        User authenticated = new User();
        authenticated.setUsername("frank");
        authenticated.setPasswordHash("old-hash");
        when(userRepository.findByUsername("frank")).thenReturn(Optional.of(authenticated));
        when(passwordEncoder.matches("old", "old-hash")).thenReturn(true);
        when(passwordEncoder.encode("new")).thenReturn("new-hash");

        var auth = new UsernamePasswordAuthenticationToken(
                "frank",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        userService.changePassword("old", "new");

        assertThat(authenticated.getPasswordHash()).isEqualTo("new-hash");
        verify(userRepository).save(authenticated);
    }

    @Test
    void changePasswordThrowsWhenCurrentPasswordInvalid() {
        User user = new User();
        user.setPasswordHash("stored");

        assertThatThrownBy(() -> userService.changePassword(user, "wrong", "new"))
                .isInstanceOf(IllegalArgumentException.class);
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createPasswordUpdatesExistingUser() throws Exception {
        User existing = new User();
        when(userRepository.findByUsername("grace")).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("pass")).thenReturn("hash");
        when(userRepository.save(existing)).thenReturn(existing);

        CreatePasswordRequest request = new CreatePasswordRequest();
        request.setUsername("grace");
        request.setPassword("pass");

        User result = userService.createPassword(request);

        assertThat(result.getPasswordHash()).isEqualTo("hash");
        assertThat(result.getRoles()).containsExactly("USER");
        assertThat(result.isEnabled()).isTrue();
        verify(userRepository).save(existing);
    }

    @Test
    void createPasswordThrowsWhenUserMissing() {
        when(userRepository.findByUsername("henry")).thenReturn(Optional.empty());

        CreatePasswordRequest request = new CreatePasswordRequest();
        request.setUsername("henry");

        assertThatThrownBy(() -> userService.createPassword(request))
                .isInstanceOf(Exception.class)
                .hasMessage("User is not found");
    }
}
