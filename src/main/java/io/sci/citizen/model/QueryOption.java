package io.sci.citizen.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="survey_parameter")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class QueryOption implements Serializable {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer sequence;
    private boolean enabled;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id")
    @NotFound(action = NotFoundAction.IGNORE)
    @JsonIgnore
    private TextQuery question;

    @Override
    public String toString() {
        return sequence + "=" + description;
    }
}
