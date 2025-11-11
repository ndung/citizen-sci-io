package io.sci.citizen.web;

import io.sci.citizen.model.User;
import io.sci.citizen.model.dto.ChangePasswordRequest;
import io.sci.citizen.model.dto.UserRequest;
import io.sci.citizen.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    @Mock
    private UserService userService;

    private ProfileController controller;

    @BeforeEach
    void setUp() {
        controller = new ProfileController(userService);
    }

    @Test
    void pwdModelReturnsEmptyChangePasswordRequest() {
        ChangePasswordRequest request = controller.pwdModel();

        assertThat(request).isNotNull();
        assertThat(request.getCurrentPassword()).isNull();
        assertThat(request.getNewPassword()).isNull();
        assertThat(request.getConfirmNewPassword()).isNull();
    }

    @Test
    void formModelLoadsCurrentUserData() {
        User user = new User();
        user.setId(7L);
        user.setUsername("demo");
        user.setEmail("demo@example.com");
        user.setFullName("Demo User");
        when(userService.getUser()).thenReturn(user);

        UserRequest request = controller.formModel();

        assertThat(request.getId()).isEqualTo(7L);
        assertThat(request.getUsername()).isEqualTo("demo");
        assertThat(request.getEmail()).isEqualTo("demo@example.com");
        assertThat(request.getFullName()).isEqualTo("Demo User");
        verify(userService).getUser();
    }

    @Test
    void viewReturnsProfileTemplate() {
        String viewName = controller.view(null);

        assertThat(viewName).isEqualTo("profile");
    }

    @Test
    void saveProfileWithExistingUsernameAddsValidationError() {
        User current = new User();
        current.setUsername("current");
        current.setEmail("current@example.com");
        when(userService.getUser()).thenReturn(current);
        when(userService.isUserNameExisted("new")).thenReturn(true);

        UserRequest form = new UserRequest();
        form.setUsername("new");
        form.setEmail("current@example.com");
        form.setFullName("Name");
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String view = controller.saveProfile(form, bindingResult, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/profile");
        assertThat(bindingResult.hasFieldErrors("username")).isTrue();
        assertThat(redirectAttributes.getFlashAttributes().get("form")).isEqualTo(form);
        assertThat(redirectAttributes.getFlashAttributes())
                .containsKey("org.springframework.validation.BindingResult.form");
        verify(userService, never()).updateProfile(any());
    }

    @Test
    void saveProfileWithExistingEmailAddsValidationError() {
        User current = new User();
        current.setUsername("current");
        current.setEmail("current@example.com");
        when(userService.getUser()).thenReturn(current);
        when(userService.isEmailExisted("new@example.com")).thenReturn(true);

        UserRequest form = new UserRequest();
        form.setUsername("current");
        form.setEmail("new@example.com");
        form.setFullName("Name");
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String view = controller.saveProfile(form, bindingResult, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/profile");
        assertThat(bindingResult.hasFieldErrors("email")).isTrue();
        assertThat(redirectAttributes.getFlashAttributes().get("form")).isEqualTo(form);
        assertThat(redirectAttributes.getFlashAttributes())
                .containsKey("org.springframework.validation.BindingResult.form");
        verify(userService, never()).updateProfile(any());
    }

    @Test
    void saveProfileWithoutErrorsUpdatesProfile() {
        User current = new User();
        current.setUsername("current");
        current.setEmail("current@example.com");
        when(userService.getUser()).thenReturn(current);

        UserRequest form = new UserRequest();
        form.setUsername("current");
        form.setEmail("current@example.com");
        form.setFullName("Name");
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String view = controller.saveProfile(form, bindingResult, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/profile");
        assertThat(bindingResult.hasErrors()).isFalse();
        assertThat(redirectAttributes.getFlashAttributes().get("profileSaved"))
                .isEqualTo(Boolean.TRUE);
        verify(userService).updateProfile(form);
    }

    @Test
    void changePasswordWithMismatchedNewPasswordAddsError() {
        ChangePasswordRequest pwd = new ChangePasswordRequest();
        pwd.setCurrentPassword("current");
        pwd.setNewPassword("new");
        pwd.setConfirmNewPassword("different");
        BindingResult bindingResult = new BeanPropertyBindingResult(pwd, "pwd");
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String view = controller.changePassword(pwd, bindingResult, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/profile");
        assertThat(bindingResult.hasFieldErrors("confirmNewPassword")).isTrue();
        assertThat(redirectAttributes.getFlashAttributes())
                .containsKey("org.springframework.validation.BindingResult.pwd");
        assertThat(redirectAttributes.getFlashAttributes().get("pwd")).isEqualTo(pwd);
        verify(userService, never()).changePassword(any(), any());
    }

    @Test
    void changePasswordWhenServiceThrowsAddsErrorToBindingResult() {
        ChangePasswordRequest pwd = new ChangePasswordRequest();
        pwd.setCurrentPassword("wrong");
        pwd.setNewPassword("new");
        pwd.setConfirmNewPassword("new");
        BindingResult bindingResult = new BeanPropertyBindingResult(pwd, "pwd");
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        doThrow(new IllegalArgumentException("Invalid current password"))
                .when(userService).changePassword("wrong", "new");

        String view = controller.changePassword(pwd, bindingResult, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/profile");
        assertThat(bindingResult.hasFieldErrors("currentPassword")).isTrue();
        assertThat(redirectAttributes.getFlashAttributes())
                .containsKey("org.springframework.validation.BindingResult.pwd");
        assertThat(redirectAttributes.getFlashAttributes().get("pwd")).isEqualTo(pwd);
    }

    @Test
    void changePasswordWithoutErrorsCallsServiceAndAddsSuccessFlash() {
        ChangePasswordRequest pwd = new ChangePasswordRequest();
        pwd.setCurrentPassword("current");
        pwd.setNewPassword("new");
        pwd.setConfirmNewPassword("new");
        BindingResult bindingResult = new BeanPropertyBindingResult(pwd, "pwd");
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String view = controller.changePassword(pwd, bindingResult, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/profile");
        assertThat(bindingResult.hasErrors()).isFalse();
        assertThat(redirectAttributes.getFlashAttributes().get("passwordChanged"))
                .isEqualTo(Boolean.TRUE);
        verify(userService).changePassword("current", "new");
    }
}
