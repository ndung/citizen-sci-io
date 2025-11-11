package io.sci.citizen.web;

import io.sci.citizen.model.Menu;
import io.sci.citizen.model.dto.MenuRequest;
import io.sci.citizen.service.MenuService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuControllerTest {

    @Mock
    private MenuService service;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private RedirectAttributes redirectAttributes;

    private MenuController controller;

    @BeforeEach
    void setUp() {
        controller = new MenuController(service);
    }

    @Test
    void listShouldPopulateItemsAndReturnListView() {
        var items = List.of("item1", "item2");
        when(service.findAllFlatOrdered()).thenReturn(items);

        String viewName = controller.list(model);

        assertEquals("menu/list", viewName);
        verify(model).addAttribute("items", items);
    }

    @Test
    void createFormShouldPrepareFormAttributes() {
        var parents = List.of("parent");
        when(service.allParentsCandidates(null)).thenReturn(parents);

        String viewName = controller.createForm(model);

        assertEquals("menu/create", viewName);

        ArgumentCaptor<MenuRequest> captor = ArgumentCaptor.forClass(MenuRequest.class);
        verify(model).addAttribute(eq("menuItem"), captor.capture());
        MenuRequest request = captor.getValue();
        assertNotNull(request);
        assertTrue(request.isEnabled());
        assertEquals(0, request.getOrderIndex());
        verify(model).addAttribute("parents", parents);
    }

    @Test
    void createShouldReturnCreateViewWhenBindingErrors() {
        MenuRequest form = new MenuRequest();
        var parents = List.of("parent");
        when(bindingResult.hasErrors()).thenReturn(true);
        when(service.allParentsCandidates(null)).thenReturn(parents);

        String viewName = controller.create(form, bindingResult, redirectAttributes, model);

        assertEquals("menu/create", viewName);
        verify(model).addAttribute("parents", parents);
        verify(service, never()).create(any());
        verifyNoInteractions(redirectAttributes);
    }

    @Test
    void createShouldPersistAndRedirectWhenValid() {
        MenuRequest form = new MenuRequest();
        when(bindingResult.hasErrors()).thenReturn(false);

        String viewName = controller.create(form, bindingResult, redirectAttributes, model);

        assertEquals("redirect:/admin/menu", viewName);
        verify(service).create(form);
        verify(redirectAttributes).addFlashAttribute("success", "Menu item created.");
        verifyNoInteractions(model);
    }

    @Test
    void editFormShouldLoadMenuAndParents() {
        long id = 5L;
        Menu parent = new Menu();
        parent.setId(10L);
        Menu entity = new Menu();
        entity.setTitle("Title");
        entity.setHref("/link");
        entity.setIcon("icon");
        entity.setOrderIndex(3);
        entity.setEnabled(false);
        entity.setParent(parent);
        Set<String> roles = new LinkedHashSet<>();
        roles.add("ROLE_ADMIN");
        roles.add("ROLE_USER");
        entity.setRequiredRoles(roles);

        when(service.getById(id)).thenReturn(entity);
        var parents = List.of("parent");
        when(service.allParentsCandidates(id)).thenReturn(parents);

        String viewName = controller.editForm(id, model);

        assertEquals("menu/edit", viewName);
        verify(model).addAttribute("menuId", id);
        ArgumentCaptor<MenuRequest> captor = ArgumentCaptor.forClass(MenuRequest.class);
        verify(model).addAttribute(eq("menu"), captor.capture());
        MenuRequest request = captor.getValue();
        assertEquals("Title", request.getTitle());
        assertEquals("/link", request.getHref());
        assertEquals("icon", request.getIcon());
        assertEquals(3, request.getOrderIndex());
        assertFalse(request.isEnabled());
        assertEquals(parent.getId(), request.getParentId());
        assertEquals("ADMIN, USER", request.getRolesCsv());
        verify(model).addAttribute("parents", parents);
    }

    @Test
    void updateShouldReturnEditViewWhenBindingErrors() {
        long id = 8L;
        MenuRequest form = new MenuRequest();
        var parents = List.of("parent");
        when(bindingResult.hasErrors()).thenReturn(true);
        when(service.allParentsCandidates(id)).thenReturn(parents);

        String viewName = controller.update(id, form, bindingResult, redirectAttributes, model);

        assertEquals("menu/edit", viewName);
        verify(model).addAttribute("menuId", id);
        verify(model).addAttribute("parents", parents);
        verify(service, never()).update(anyLong(), any());
        verifyNoInteractions(redirectAttributes);
    }

    @Test
    void updateShouldPersistAndRedirectWhenValid() {
        long id = 9L;
        MenuRequest form = new MenuRequest();
        when(bindingResult.hasErrors()).thenReturn(false);

        String viewName = controller.update(id, form, bindingResult, redirectAttributes, model);

        assertEquals("redirect:/admin/menu", viewName);
        verify(service).update(id, form);
        verify(redirectAttributes).addFlashAttribute("success", "Menu item updated.");
        verifyNoInteractions(model);
    }

    @Test
    void deleteShouldRemoveMenuAndRedirect() {
        long id = 4L;

        String viewName = controller.delete(id, redirectAttributes);

        assertEquals("redirect:/admin/menu", viewName);
        verify(service).delete(id);
        verify(redirectAttributes).addFlashAttribute("success", "Menu item deleted.");
    }
}
