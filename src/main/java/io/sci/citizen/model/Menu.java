package io.sci.citizen.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "menu")
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    // Null = non-clickable header/accordion
    @Column(length = 255)
    private String href;

    @Column(length = 80)
    private String icon; // optional (e.g., "ki-filled ki-home")

    private Integer orderIndex = 0;

    private boolean enabled = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Menu parent;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "menu_roles", joinColumns = @JoinColumn(name = "menu_id"))
    @Column(name = "role")
    private Set<String> requiredRoles = new LinkedHashSet<>();

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getHref() { return href; }
    public void setHref(String href) { this.href = href; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Menu getParent() { return parent; }
    public void setParent(Menu parent) { this.parent = parent; }
    public Set<String> getRequiredRoles() { return requiredRoles; }
    public void setRequiredRoles(Set<String> requiredRoles) { this.requiredRoles = requiredRoles; }

    /** Child collection (inverse side), ordered by orderIndex then id */
    @OneToMany(mappedBy = "parent",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC, id ASC")
    // @JsonManagedReference // <- with Jackson, pairs with @JsonBackReference
    private List<Menu> children = new ArrayList<>();
    public List<Menu> getChildren() { return children; }
    public void setChildren(List<Menu> children) { this.children = children; }

}