package io.sci.citizen.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.util.Date;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "project")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Project {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;
    private String icon;
    @Column(length = 500)
    private String description;

    @Column(nullable = false, updatable = false)
    private Date createdAt = new Date();

    @JsonIgnore
    private boolean enabled = true;

    @JsonIgnore
    private boolean publiclyAvailable = false;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private User creator;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy="project", cascade = {CascadeType.ALL})
    private List<Section> sections;

    @Transient
    public String getIconUrl(){
        return "https://citizen-sci-io-c296af702ec9.herokuapp.com/files/"+icon;
    }
}