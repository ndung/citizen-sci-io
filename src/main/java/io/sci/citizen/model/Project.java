package io.sci.citizen.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "project")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;
    private String icon;
    @Column(length = 500)
    private String description;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

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

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isPubliclyAvailable() {
        return publiclyAvailable;
    }

    public void setPubliclyAvailable(boolean publiclyAvailable) {
        this.publiclyAvailable = publiclyAvailable;
    }

    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    @Transient
    public String getIconUrl(){
        return "https://citizen-sci-io-c296af702ec9.herokuapp.com/"+icon;
    }
}