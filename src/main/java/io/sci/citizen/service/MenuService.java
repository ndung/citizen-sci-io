package io.sci.citizen.service;

import io.sci.citizen.model.Menu;
import io.sci.citizen.model.dto.MenuRequest;
import io.sci.citizen.model.repository.MenuRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MenuService {

    private final MenuRepository repo;

    public MenuService(MenuRepository repo) { this.repo = repo; }

    @Transactional(readOnly = true)
    public List<Menu> findAllFlatOrdered() {
        return repo.findAllByOrderByParentIdAscOrderIndexAscTitleAsc();
    }

    @Transactional(readOnly = true)
    public List<Menu> allParentsCandidates(Long excludeId) {
        var all = repo.findAllByOrderByParentIdAscOrderIndexAscTitleAsc();
        if (excludeId == null) return all;
        // prevent self as parent (naive – for deep cycle prevention, add more checks)
        return all.stream().filter(m -> !m.getId().equals(excludeId)).toList();
    }

    @Transactional(readOnly = true)
    public Menu getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Menu create(MenuRequest req) {
        Menu e = new Menu();
        apply(e, req);
        return repo.save(e);
    }

    @Transactional
    public Menu update(Long id, MenuRequest req) {
        var e = getById(id);
        apply(e, req);
        return repo.save(e);
    }

    @Transactional
    public void delete(Long id) {
        // optionally ensure no children exist before delete
        repo.deleteById(id);
    }

    private void apply(Menu e, MenuRequest req) {
        e.setTitle(req.getTitle());
        e.setHref(req.getHref());
        e.setIcon(req.getIcon());
        e.setOrderIndex(req.getOrderIndex() == null ? 0 : req.getOrderIndex());
        e.setEnabled(req.isEnabled());
        e.setRequiredRoles(req.rolesAsSet());

        if (req.getParentId() != null) {
            var p = repo.findById(req.getParentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent not found"));
            if (e.getId() != null && p.getId().equals(e.getId()))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot set self as parent");
            e.setParent(p);
        } else {
            e.setParent(null);
        }
    }

    public boolean hasAny(Set<String> required, Set<String> userRoles) {
        Set<String> req = normalizeRoles(required);
        if (req.isEmpty()) return true;                  // no restriction
        Set<String> user = normalizeRoles(userRoles);
        for (String r : req) if (user.contains(r)) return true;
        return false;
    }

    public Menu filterGroup(Menu group, Set<String> roles) {
        if (group == null || !group.isEnabled()) return null;

        // If this node itself is not allowed, drop it
        if (!hasAny(group.getRequiredRoles(), roles)) return null;

        // Load children from DB and filter them recursively
        List<Menu> filteredChildren = childrenOf(group).stream()
                .map(child -> filterGroup(child, roles))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        boolean isLeaf = group.getHref() != null && !group.getHref().isBlank();
        if (!isLeaf && filteredChildren.isEmpty()) {
            // hide empty accordion/header
            return null;
        }

        // Shallow copy into a detached node (so we don't mutate managed entities)
        Menu copy = new Menu();
        copy.setId(group.getId());                // optional: keep id for links/anchors
        copy.setTitle(group.getTitle());
        copy.setHref(group.getHref());
        copy.setIcon(group.getIcon());
        copy.setOrderIndex(group.getOrderIndex());
        copy.setEnabled(group.isEnabled());
        copy.setRequiredRoles(group.getRequiredRoles() != null
                ? new LinkedHashSet<>(group.getRequiredRoles())
                : new LinkedHashSet<>());
        copy.setParent(null);                     // avoid cycles in the returned tree
        copy.setChildren(filteredChildren);       // uses @Transient field
        return copy;
    }

    /** Build the permitted top-level menu for the current user. */
    public List<Menu> allowedFor(Authentication auth) {
        if (auth == null || auth.getAuthorities() == null) return List.of();

        Set<String> userRoles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return repo.findAllByParentIsNullOrderByOrderIndexAscTitleAsc().stream()
                .map(top -> filterGroup(top, userRoles))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // ----------------- helpers -----------------

    private List<Menu> childrenOf(Menu parent) {
        return repo.findAllByParentOrderByOrderIndexAscTitleAsc(parent);
    }

    /** Normalize roles: trim, uppercase, strip ROLE_ prefix; null-safe. */
    private static Set<String> normalizeRoles(Collection<String> roles) {
        if (roles == null) return Set.of();
        return roles.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toUpperCase(Locale.ROOT))
                .map(s -> s.startsWith("ROLE_") ? s.substring(5) : s)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
