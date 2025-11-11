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
@Table(name="survey_question")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TextQuery implements Serializable {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String attribute;

    private String question;

    private Integer type;

    private boolean enabled;

    private Integer sequence;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private Section section;

    private boolean required;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy="question", cascade = {CascadeType.ALL})
    private List<QueryOption> options;

}
