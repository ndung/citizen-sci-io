package io.sci.citizen.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name="survey_question")
public class TextQuery implements Serializable {

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAttribute() { return attribute; }

    public void setAttribute(String attribute) { this.attribute = attribute; }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getSequence() { return sequence; }

    public void setSequence(Integer sequence) { this.sequence = sequence; }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public List<QueryOption> getOptions() {
        return options;
    }

    public void setOptions(List<QueryOption> options) {
        this.options = options;
    }

    public Section getSection() { return section; }

    public void setSection(Section section) { this.section = section; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TextQuery textQuery = (TextQuery) o;
        return Objects.equals(id, textQuery.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
