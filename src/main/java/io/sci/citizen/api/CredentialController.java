package io.sci.citizen.api;

import io.sci.citizen.api.dto.SignInRequest;
import io.sci.citizen.model.User;
import io.sci.citizen.model.dto.CreatePasswordRequest;
import io.sci.citizen.api.dto.LoginDetails;
import io.sci.citizen.model.dto.ChangePasswordRequest;
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
@RequestMapping("/api/auth")
public class CredentialController extends BaseApiController {

    @Autowired
    private AuthenticationManager authManager;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private UserService userService;
    @Autowired
    private ProjectService projectService;

    @RequestMapping(value = "/check-credential-id", method = RequestMethod.POST)
    public ResponseEntity<Response> checkUserName(@RequestBody String username) {
        System.out.printf("username: %s\n", username);
        int result = 0;
        User user = userService.getUser(username);
        if (user==null) {
            result = -1;
        }else {
            if (user.isEnabled()){
                result = 1;
            }
        }
        return getHttpStatus(new Response(result));
    }

    @RequestMapping(value = "/create-password", method = RequestMethod.POST)
    public ResponseEntity<Response> createPassword(@RequestBody CreatePasswordRequest createPasswordRequest) {
        try {
            User user = userService.createPassword(createPasswordRequest);
            LoginDetails details = createLoginDetails(user);
            String token = createToken(user);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Token", token);
            return getHttpStatus(new Response(details), responseHeaders);
        } catch (Exception e) {
            e.printStackTrace();
            return getHttpStatus(new Response(e.getMessage()));
        }
    }

    @RequestMapping(value = "/sign-in", method = RequestMethod.POST)
    public ResponseEntity<Response> signIn(@RequestBody SignInRequest req) {
        try {
            Authentication auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(req.username(), req.password()));
            var userDetails = userDetailsService.loadUserByUsername(req.password());
            if (userDetails!=null) {
                User user = userService.getUser(userDetails.getUsername());
                LoginDetails details = createLoginDetails(user);
                String token = createToken(user);
                HttpHeaders responseHeaders = new HttpHeaders();
                responseHeaders.set("Token", token);
                return getHttpStatus(new Response(details), responseHeaders);
            }else{
                return response(HttpStatus.UNAUTHORIZED, new Response());
            }
        } catch (Exception e) {
            return getHttpStatus(new Response(e.getMessage()));
        }
    }

    @RequestMapping(value = "/sign-up", method = RequestMethod.POST)
    public ResponseEntity<Response> signUp(@RequestBody UserRequest request) {
        try {
            User user = userService.signUp(request);
            LoginDetails details = createLoginDetails(user);
            String token = createToken(user);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Token", token);
            return getHttpStatus(new Response(details), responseHeaders);
        } catch (Exception e) {
            e.printStackTrace();
            return getHttpStatus(new Response(e.getMessage()));
        }
    }

    private LoginDetails createLoginDetails(User user){
        return new LoginDetails(user, projectService.findAll(user.getId()));
    }

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

