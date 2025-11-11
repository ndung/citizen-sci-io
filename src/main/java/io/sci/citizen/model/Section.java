package io.sci.citizen.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.io.Serializable;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="section")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Section implements Serializable {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int sequence;

    private String type;

    private String name;

    private boolean enabled = true;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private Project project;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy="section", cascade = {CascadeType.ALL})
    private List<TextQuery> questions;

}
