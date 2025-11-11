package io.sci.citizen.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.util.Date;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="survey_response")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class QueryReply {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "data_id")
    @NotFound(action = NotFoundAction.IGNORE)
    @JsonIgnore
    private Data data;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private TextQuery question;

    @Column(name = "response")
    private String response;

    @Column(name = "date_time")
    private Date responseDateTime;

}
