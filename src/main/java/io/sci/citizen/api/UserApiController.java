package io.sci.citizen.api;

import io.sci.citizen.api.dto.LoginDetails;
import io.sci.citizen.api.dto.SignInRequest;
import io.sci.citizen.model.User;
import io.sci.citizen.model.dto.ChangePasswordRequest;
import io.sci.citizen.model.dto.CreatePasswordRequest;
import io.sci.citizen.model.dto.UserRequest;
import io.sci.citizen.service.ProjectService;
import io.sci.citizen.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserApiController extends BaseApiController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/change-pwd", method = RequestMethod.POST)
    public ResponseEntity<Response> changePassword(@RequestHeader("Authorization") String token,
                                                   @RequestBody ChangePasswordRequest request) {
        try {
            if (!authorize(token)) {
                return FORBIDDEN;
            }

            String userId = getUserId(token);
            User user = userService.getById(Long.parseLong(userId));
            userService.changePassword(user, request.getCurrentPassword(), request.getNewPassword());
            return getHttpStatus(new Response(user));

        } catch (Exception e) {
            return getHttpStatus(new Response(e.getMessage()));
        }
    }

    @RequestMapping(value = "/update-profile", method = RequestMethod.POST)
    public ResponseEntity<Response> changeProfile(@RequestHeader("Authorization") String token,
                                                  @RequestBody UserRequest request) {
        try {
            if (!authorize(token)) {
                return FORBIDDEN;
            }

            String userId = getUserId(token);
            User user = userService.getById(Long.parseLong(userId));
            user = userService.updateProfile(user, request);
            return getHttpStatus(new Response(user));

        } catch (Exception e) {
            return getHttpStatus(new Response(e.getMessage()));
        }
    }

}

