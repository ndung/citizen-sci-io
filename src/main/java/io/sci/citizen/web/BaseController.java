package io.sci.citizen.web;

import io.sci.citizen.model.Project;
import io.sci.citizen.model.User;
import io.sci.citizen.service.ProjectService;
import io.sci.citizen.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class BaseController {

    @Autowired
    protected UserService userService;

    @Autowired
    protected ProjectService projectService;

    private boolean isAdmin(){
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    public boolean isAuthorized(Project project){
        if (isAdmin()) return true;
        if (project==null) return false;
        User user = getUser();
        return Objects.equals(project.getCreator().getId(), user.getId());
    }

    public boolean isAuthorized(Long projectId){
        if (isAdmin()) return true;
        Project project = projectService.getById(projectId);
        return isAuthorized(project);
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
            return userService.getUser(username);
        }
        return null;
    }
}
