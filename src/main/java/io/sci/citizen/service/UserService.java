package io.sci.citizen.service;

import io.sci.citizen.api.component.PasswordUtil;
import io.sci.citizen.model.User;
import io.sci.citizen.model.dto.CreatePasswordRequest;
import io.sci.citizen.model.dto.UserRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService extends BaseService {

    private final PasswordEncoder encoder;

    public UserService(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepo.findAll(Sort.by("id").descending());
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public boolean isUserNameExisted(String username){
        return userRepo.existsByUsername(username);
    }

    public boolean isEmailExisted(String email){
        return userRepo.existsByEmail(email);
    }

    @Transactional
    public User create(UserRequest req) {
        var u = new User();
        u.setUsername(req.getUsername().trim());
        u.setFullName(req.getFullName().trim());
        u.setEmail(req.getEmail().trim());
        u.setEnabled(req.isEnabled());
        u.setRoles(req.rolesAsSet());
        u.setPasswordHash(encoder.encode(req.getPassword()));
        return userRepo.save(u);
    }

    @Transactional
    public User signUp(UserRequest req) {
        var u = new User();
        u.setUsername(req.getUsername().trim());
        u.setFullName(req.getFullName().trim());
        u.setEmail(req.getEmail().trim());
        u.setEnabled(true);
        u.setRoles(Set.of("USER"));
        u.setPasswordHash(encoder.encode(req.getPassword()));
        return userRepo.save(u);
    }

    @Transactional
    public User update(Long id, UserRequest req) {
        var u = getById(id);
        u.setUsername(req.getUsername().trim());
        u.setFullName(req.getFullName().trim());
        u.setEmail(req.getEmail().trim());
        u.setEnabled(req.isEnabled());
        u.setRoles(req.rolesAsSet());

        // Update password only if provided
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            u.setPasswordHash(encoder.encode(req.getPassword()));
        }

        return userRepo.save(u);
    }


    @Transactional
    public User updateProfile(UserRequest req) {
        var u = getUser();
        return updateProfile(u, req);
    }

    @Transactional
    public User updateProfile(User u, UserRequest req) {
        u.setUsername(req.getUsername().trim());
        u.setFullName(req.getFullName().trim());
        u.setEmail(req.getEmail().trim());
        return userRepo.save(u);
    }

    @Transactional
    public void changePassword(String currentPassword, String newPassword) {
        var u = getUser();
        changePassword(u, currentPassword, newPassword);
    }

    @Transactional
    public void changePassword(User u, String currentPassword, String newPassword) {
        if (!encoder.matches(currentPassword, u.getPasswordHash())){
            throw new IllegalArgumentException("Old password does not match");
        }
        u.setPasswordHash(encoder.encode(newPassword));
        userRepo.save(u);
    }

    @Transactional
    public User createPassword(CreatePasswordRequest request) throws Exception {
        Optional<User> opt = userRepo.findByUsername(request.getUsername());
        if (opt.isEmpty()) {
            throw new Exception("User is not found");
        }
        //if (!isPasswordValid(request.getPassword(), true, true, 6, 20)) {
        //    throw new Exception("Kata sandi tidak valid");
        //}
        User  user = opt.get();
        user.setRoles(Set.of("USER"));
        user.setPasswordHash(PasswordUtil.md5Hash(request.getPassword()));
        user.setEnabled(true);
        return userRepo.save(user);
    }
}