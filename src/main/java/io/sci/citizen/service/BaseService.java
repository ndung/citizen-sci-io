package io.sci.citizen.service;

import io.sci.citizen.model.User;
import io.sci.citizen.model.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class BaseService {

    @Autowired
    protected UserRepository userRepo;

    public boolean isAdmin(){
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    public String getUsername(){
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : null;
    }

    public User getUser(){
        String username = getUsername();
        return getUser(username);
    }

    public User getUser(String username){
        if (username!=null) {
            Optional<User> opt = userRepo.findByUsername(username);
            if (opt.isPresent()) {
                return opt.get();
            }
        }
        return null;
    }
}
