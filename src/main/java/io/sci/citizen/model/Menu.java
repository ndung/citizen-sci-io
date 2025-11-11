package io.sci.citizen.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "menu")
@Getter
@Setter
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

    /** Child collection (inverse side), ordered by orderIndex then id */
    @OneToMany(mappedBy = "parent",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC, id ASC")
    // @JsonManagedReference // <- with Jackson, pairs with @JsonBackReference
    private List<Menu> children = new ArrayList<>();

}
