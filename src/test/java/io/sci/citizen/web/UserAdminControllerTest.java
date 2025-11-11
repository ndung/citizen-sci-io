package io.sci.citizen.web;

import io.sci.citizen.model.User;
import io.sci.citizen.model.dto.UserRequest;
import io.sci.citizen.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAdminControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserAdminController controller;

    private Model model;

    @BeforeEach
    void setUp() {
        model = new ExtendedModelMap();
    }

    @Test
    void listWithoutUserIdPopulatesEmptyFormAndUsers() {
        var users = List.of(new User(), new User());
        when(userService.findAll()).thenReturn(users);

        String view = controller.list(null, model);

        assertThat(view).isEqualTo("users");
        assertThat(model.getAttribute("user")).isInstanceOf(UserRequest.class);
        assertThat(model.getAttribute("users")).isEqualTo(users);
        verify(userService, never()).getById(any());
        verify(userService).findAll();
    }

    @Test
    void listWithUserIdPrepopulatesFormFromService() {
        var user = new User();
        user.setId(5L);
        user.setFullName("Alice");
        user.setEnabled(true);
        user.setEmail("alice@example.com");
        user.setUsername("alice");
        user.setRoles(Set.of("ADMIN", "USER"));
        when(userService.getById(5L)).thenReturn(user);
        when(userService.findAll()).thenReturn(List.of(user));

        String view = controller.list(5L, model);

        assertThat(view).isEqualTo("users");
        UserRequest form = (UserRequest) model.getAttribute("user");
        assertThat(form.getId()).isEqualTo(5L);
        assertThat(form.getFullName()).isEqualTo("Alice");
        assertThat(form.getEmail()).isEqualTo("alice@example.com");
        assertThat(form.getUsername()).isEqualTo("alice");
        assertThat(form.getRolesCsv()).contains("ADMIN");
        assertThat(model.getAttribute("users")).isEqualTo(List.of(user));
    }

    @Test
    void createWhenBindingHasErrorsReturnsUsersView() {
        UserRequest form = minimalCreateForm();
        BindingResult binding = binding(form);
        binding.reject("error");
        RedirectAttributes ra = new RedirectAttributesModelMap();
        when(userService.findAll()).thenReturn(List.of());

        String view = controller.create(form, binding, ra, model);

        assertThat(view).isEqualTo("users");
        verify(userService, never()).create(any());
    }

    @Test
    void createWhenUsernameExistsAddsError() {
        UserRequest form = minimalCreateForm();
        BindingResult binding = binding(form);
        RedirectAttributes ra = new RedirectAttributesModelMap();
        when(userService.findAll()).thenReturn(List.of());
        when(userService.isUserNameExisted("alice"))
                .thenReturn(true);

        String view = controller.create(form, binding, ra, model);

        assertThat(view).isEqualTo("users");
        assertThat(binding.getFieldError("username")).isNotNull();
        verify(userService, never()).create(any());
    }

    @Test
    void createWhenEmailExistsAddsError() {
        UserRequest form = minimalCreateForm();
        BindingResult binding = binding(form);
        RedirectAttributes ra = new RedirectAttributesModelMap();
        when(userService.findAll()).thenReturn(List.of());
        when(userService.isUserNameExisted("alice")).thenReturn(false);
        when(userService.isEmailExisted("alice@example.com")).thenReturn(true);

        String view = controller.create(form, binding, ra, model);

        assertThat(view).isEqualTo("users");
        assertThat(binding.getFieldError("email")).isNotNull();
        verify(userService, never()).create(any());
    }

    @Test
    void createWhenPasswordMissingAddsError() {
        UserRequest form = minimalCreateForm();
        form.setPassword(" ");
        BindingResult binding = binding(form);
        RedirectAttributes ra = new RedirectAttributesModelMap();
        when(userService.findAll()).thenReturn(List.of());
        when(userService.isUserNameExisted("alice")).thenReturn(false);
        when(userService.isEmailExisted("alice@example.com")).thenReturn(false);

        String view = controller.create(form, binding, ra, model);

        assertThat(view).isEqualTo("users");
        assertThat(binding.getFieldError("password")).isNotNull();
        verify(userService, never()).create(any());
    }

    @Test
    void createWhenPasswordsDoNotMatchAddsError() {
        UserRequest form = minimalCreateForm();
        form.setConfirmPassword("different");
        BindingResult binding = binding(form);
        RedirectAttributes ra = new RedirectAttributesModelMap();
        when(userService.findAll()).thenReturn(List.of());
        when(userService.isUserNameExisted("alice")).thenReturn(false);
        when(userService.isEmailExisted("alice@example.com")).thenReturn(false);

        String view = controller.create(form, binding, ra, model);

        assertThat(view).isEqualTo("users");
        assertThat(binding.getFieldError("confirmPassword")).isNotNull();
        verify(userService, never()).create(any());
    }

    @Test
    void createSuccessRedirectsAndSavesUser() {
        UserRequest form = minimalCreateForm();
        BindingResult binding = binding(form);
        RedirectAttributes ra = new RedirectAttributesModelMap();
        when(userService.findAll()).thenReturn(List.of());

        String view = controller.create(form, binding, ra, model);

        assertThat(view).isEqualTo("redirect:/admin/users");
        assertThat(ra.getFlashAttributes()).containsKey("userSaved");
        verify(userService).create(eq(form));
    }

    @Test
    void editFormRedirectsToListWithUserId() {
        String view = controller.editForm(42L);

        assertThat(view).isEqualTo("redirect:/admin/users?userId=42");
    }

    @Test
    void updateWhenBindingHasErrorsReturnsUsersView() {
        UserRequest form = minimalCreateForm();
        BindingResult binding = binding(form);
        binding.reject("error");
        RedirectAttributes ra = new RedirectAttributesModelMap();
        when(userService.findAll()).thenReturn(List.of());

        String view = controller.update(1L, form, binding, ra, model);

        assertThat(view).isEqualTo("users");
        verify(userService, never()).update(any(), any());
    }

    @Test
    void updateWhenUsernameExistsAddsError() {
        UserRequest form = minimalCreateForm();
        form.setUsername("bob");
        BindingResult binding = binding(form);
        RedirectAttributes ra = new RedirectAttributesModelMap();
        var existing = new User();
        existing.setUsername("alice");
        existing.setEmail("alice@example.com");
        when(userService.findAll()).thenReturn(List.of());
        when(userService.getById(1L)).thenReturn(existing);
        when(userService.isUserNameExisted("bob")).thenReturn(true);

        String view = controller.update(1L, form, binding, ra, model);

        assertThat(view).isEqualTo("users");
        assertThat(binding.getFieldError("username")).isNotNull();
        verify(userService, never()).update(any(), any());
    }

    @Test
    void updateWhenEmailExistsAddsError() {
        UserRequest form = minimalCreateForm();
        form.setEmail("bob@example.com");
        BindingResult binding = binding(form);
        RedirectAttributes ra = new RedirectAttributesModelMap();
        var existing = new User();
        existing.setUsername("alice");
        existing.setEmail("alice@example.com");
        when(userService.findAll()).thenReturn(List.of());
        when(userService.getById(1L)).thenReturn(existing);
        when(userService.isEmailExisted("bob@example.com")).thenReturn(true);

        String view = controller.update(1L, form, binding, ra, model);

        assertThat(view).isEqualTo("users");
        assertThat(binding.getFieldError("email")).isNotNull();
        verify(userService, never()).update(any(), any());
    }

    @Test
    void updateWhenPasswordsDoNotMatchAddsError() {
        UserRequest form = minimalCreateForm();
        form.setPassword("new-pass");
        form.setConfirmPassword("different");
        BindingResult binding = binding(form);
        RedirectAttributes ra = new RedirectAttributesModelMap();
        var existing = new User();
        existing.setUsername("alice");
        existing.setEmail("alice@example.com");
        when(userService.findAll()).thenReturn(List.of());
        when(userService.getById(1L)).thenReturn(existing);

        String view = controller.update(1L, form, binding, ra, model);

        assertThat(view).isEqualTo("users");
        assertThat(binding.getFieldError("confirmPassword")).isNotNull();
        verify(userService, never()).update(any(), any());
    }

    @Test
    void updateSuccessRedirectsAndSavesUser() {
        UserRequest form = minimalCreateForm();
        form.setPassword("");
        form.setConfirmPassword("");
        BindingResult binding = binding(form);
        RedirectAttributes ra = new RedirectAttributesModelMap();
        var existing = new User();
        existing.setUsername("alice");
        existing.setEmail("alice@example.com");
        when(userService.findAll()).thenReturn(List.of());
        when(userService.getById(1L)).thenReturn(existing);

        String view = controller.update(1L, form, binding, ra, model);

        assertThat(view).isEqualTo("redirect:/admin/users");
        assertThat(ra.getFlashAttributes()).containsKey("userSaved");
        verify(userService).update(1L, form);
    }

    private static UserRequest minimalCreateForm() {
        UserRequest form = new UserRequest();
        form.setUsername("alice");
        form.setFullName("Alice");
        form.setEmail("alice@example.com");
        form.setEnabled(true);
        form.setRolesCsv("ADMIN, USER");
        form.setPassword("secret");
        form.setConfirmPassword("secret");
        return form;
    }

    private static BindingResult binding(UserRequest form) {
        return new BeanPropertyBindingResult(form, "user");
    }
}
