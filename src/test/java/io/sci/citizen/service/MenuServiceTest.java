package io.sci.citizen.service;

import io.sci.citizen.model.Menu;
import io.sci.citizen.model.dto.MenuRequest;
import io.sci.citizen.model.repository.MenuRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuRepository repo;

    @InjectMocks
    private MenuService service;

    @Test
    void findAllFlatOrderedDelegatesToRepository() {
        List<Menu> menus = List.of(new Menu(), new Menu());
        when(repo.findAllByOrderByParentIdAscOrderIndexAscTitleAsc()).thenReturn(menus);

        List<Menu> result = service.findAllFlatOrdered();

        assertThat(result).isSameAs(menus);
        verify(repo).findAllByOrderByParentIdAscOrderIndexAscTitleAsc();
    }

    @Test
    void allParentsCandidatesWhenExcludeNullReturnsAll() {
        List<Menu> menus = List.of(menuWithId(1L), menuWithId(2L));
        when(repo.findAllByOrderByParentIdAscOrderIndexAscTitleAsc()).thenReturn(menus);

        List<Menu> result = service.allParentsCandidates(null);

        assertThat(result).containsExactlyElementsOf(menus);
    }

    @Test
    void allParentsCandidatesFiltersOutProvidedId() {
        Menu keep = menuWithId(1L);
        Menu remove = menuWithId(2L);
        when(repo.findAllByOrderByParentIdAscOrderIndexAscTitleAsc()).thenReturn(List.of(keep, remove));

        List<Menu> result = service.allParentsCandidates(2L);

        assertThat(result).containsExactly(keep);
    }

    @Test
    void getByIdReturnsEntityWhenPresent() {
        Menu menu = menuWithId(42L);
        when(repo.findById(42L)).thenReturn(Optional.of(menu));

        Menu result = service.getById(42L);

        assertThat(result).isSameAs(menu);
    }

    @Test
    void getByIdThrowsWhenMissing() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createPersistsMenuWithMappedFields() {
        MenuRequest request = new MenuRequest();
        request.setTitle("Dashboard");
        request.setHref("/dash");
        request.setIcon("home");
        request.setOrderIndex(5);
        request.setEnabled(true);
        request.setRolesCsv("ROLE_ADMIN, user");
        request.setParentId(10L);

        Menu parent = menuWithId(10L);
        when(repo.findById(10L)).thenReturn(Optional.of(parent));
        when(repo.save(any(Menu.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Menu result = service.create(request);

        ArgumentCaptor<Menu> captor = ArgumentCaptor.forClass(Menu.class);
        verify(repo).save(captor.capture());
        Menu saved = captor.getValue();

        assertThat(saved.getId()).isNull();
        assertThat(saved.getTitle()).isEqualTo("Dashboard");
        assertThat(saved.getHref()).isEqualTo("/dash");
        assertThat(saved.getIcon()).isEqualTo("home");
        assertThat(saved.getOrderIndex()).isEqualTo(5);
        assertThat(saved.isEnabled()).isTrue();
        assertThat(saved.getParent()).isSameAs(parent);
        assertThat(saved.getRequiredRoles()).containsExactlyInAnyOrder("ADMIN", "user");
        assertThat(result).isSameAs(saved);
    }

    @Test
    void createDefaultsOrderIndexToZeroWhenNull() {
        MenuRequest request = new MenuRequest();
        request.setTitle("Untitled");
        request.setOrderIndex(null);

        when(repo.save(any(Menu.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Menu result = service.create(request);

        assertThat(result.getOrderIndex()).isZero();
    }

    @Test
    void updateAppliesChangesOnExistingEntity() {
        Menu existing = menuWithId(25L);
        when(repo.findById(25L)).thenReturn(Optional.of(existing));
        when(repo.save(existing)).thenReturn(existing);

        MenuRequest request = new MenuRequest();
        request.setTitle("Updated");
        request.setHref("/updated");
        request.setIcon("icon");
        request.setOrderIndex(3);
        request.setEnabled(false);
        request.setRolesCsv("ADMIN");

        Menu result = service.update(25L, request);

        assertThat(existing.getTitle()).isEqualTo("Updated");
        assertThat(existing.getHref()).isEqualTo("/updated");
        assertThat(existing.getIcon()).isEqualTo("icon");
        assertThat(existing.getOrderIndex()).isEqualTo(3);
        assertThat(existing.isEnabled()).isFalse();
        assertThat(existing.getRequiredRoles()).containsExactly("ADMIN");
        assertThat(result).isSameAs(existing);
    }

    @Test
    void updateRejectsSelfAsParent() {
        Menu existing = menuWithId(30L);
        when(repo.findById(30L)).thenReturn(Optional.of(existing));

        MenuRequest request = new MenuRequest();
        request.setTitle("Self");
        request.setParentId(30L);

        when(repo.findById(30L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.update(30L, request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void deleteDelegatesToRepository() {
        service.delete(5L);
        verify(repo).deleteById(5L);
    }

    @Nested
    class HasAnyTests {
        @Test
        void returnsTrueWhenRequiredEmpty() {
            assertThat(service.hasAny(Set.of(), Set.of("ADMIN"))).isTrue();
        }

        @Test
        void matchesRolesIgnoringCaseAndPrefix() {
            Set<String> required = Set.of("admin", "manager");
            Set<String> user = Set.of("ROLE_MANAGER");

            assertThat(service.hasAny(required, user)).isTrue();
        }

        @Test
        void returnsFalseWhenNoMatch() {
            assertThat(service.hasAny(Set.of("ADMIN"), Set.of("USER"))).isFalse();
        }
    }

    @Nested
    class FilterGroupTests {
        @Test
        void returnsNullWhenGroupDisabled() {
            Menu group = new Menu();
            group.setEnabled(false);

            assertThat(service.filterGroup(group, Set.of("ADMIN"))).isNull();
        }

        @Test
        void returnsNullWhenGroupNotPermitted() {
            Menu group = new Menu();
            group.setEnabled(true);
            group.setRequiredRoles(Set.of("ADMIN"));

            assertThat(service.filterGroup(group, Set.of("USER"))).isNull();
        }

        @Test
        void hidesNonLeafWithoutAllowedChildren() {
            Menu group = new Menu();
            group.setTitle("Group");
            group.setEnabled(true);
            when(repo.findAllByParentOrderByOrderIndexAscTitleAsc(group)).thenReturn(List.of());

            assertThat(service.filterGroup(group, Set.of())).isNull();
        }

        @Test
        void returnsCopyWithFilteredChildren() {
            Menu group = new Menu();
            group.setId(1L);
            group.setTitle("Admin");
            group.setEnabled(true);
            group.setRequiredRoles(Set.of("ADMIN"));

            Menu allowedChild = new Menu();
            allowedChild.setId(2L);
            allowedChild.setHref("/allowed");
            allowedChild.setEnabled(true);
            allowedChild.setRequiredRoles(Set.of("ADMIN"));

            Menu deniedChild = new Menu();
            deniedChild.setId(3L);
            deniedChild.setHref("/denied");
            deniedChild.setEnabled(true);
            deniedChild.setRequiredRoles(Set.of("USER"));

            when(repo.findAllByParentOrderByOrderIndexAscTitleAsc(group)).thenReturn(List.of(allowedChild, deniedChild));
            when(repo.findAllByParentOrderByOrderIndexAscTitleAsc(allowedChild)).thenReturn(List.of());

            Menu result = service.filterGroup(group, Set.of("ADMIN"));

            assertThat(result).isNotSameAs(group);
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("Admin");
            assertThat(result.getRequiredRoles()).containsExactly("ADMIN");
            assertThat(result.getChildren()).hasSize(1);
            assertThat(result.getChildren().get(0).getId()).isEqualTo(2L);
            assertThat(result.getChildren().get(0)).isNotSameAs(allowedChild);
        }
    }

    @Test
    void allowedForBuildsTreeForPermittedUser() {
        Menu top1 = new Menu();
        top1.setId(1L);
        top1.setEnabled(true);
        top1.setRequiredRoles(Set.of("ADMIN"));

        Menu top2 = new Menu();
        top2.setId(2L);
        top2.setEnabled(true);
        top2.setRequiredRoles(Set.of("USER"));

        when(repo.findAllByParentIsNullOrderByOrderIndexAscTitleAsc()).thenReturn(List.of(top1, top2));
        when(repo.findAllByParentOrderByOrderIndexAscTitleAsc(top1)).thenReturn(List.of());
        when(repo.findAllByParentOrderByOrderIndexAscTitleAsc(top2)).thenReturn(List.of());

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user",
                "pwd",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        List<Menu> result = service.allowedFor(auth);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    private static Menu menuWithId(Long id) {
        Menu menu = new Menu();
        menu.setId(id);
        menu.setEnabled(true);
        menu.setRequiredRoles(new LinkedHashSet<>());
        return menu;
    }
}
