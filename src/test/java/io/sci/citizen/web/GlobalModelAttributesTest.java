package io.sci.citizen.web;

import io.sci.citizen.model.Menu;
import io.sci.citizen.service.MenuService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalModelAttributesTest {

    @Mock
    private MenuService menuService;

    @InjectMocks
    private GlobalModelAttributes globalModelAttributes;

    @Test
    void menuItemsShouldDelegateToMenuService() {
        Authentication authentication = mock(Authentication.class);
        List<Menu> expectedMenus = List.of(new Menu());
        when(menuService.allowedFor(authentication)).thenReturn(expectedMenus);

        List<Menu> actualMenus = globalModelAttributes.menuItems(authentication);

        assertSame(expectedMenus, actualMenus);
        verify(menuService).allowedFor(authentication);
    }
}
