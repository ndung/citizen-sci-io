package io.sci.citizen.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.util.Date;

@Entity
@Table(name="survey_response")
public class QueryReply {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "data_id")
    @NotFound(action = NotFoundAction.IGNORE)
    @JsonIgnore
    private Data data;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "question_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private TextQuery question;
    @Column(name = "response")
    private String response;
    @Column(name = "date_time")
    private Date responseDateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public TextQuery getQuestion() { return question; }

    public void setQuestion(TextQuery question) { this.question = question; }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Date getResponseDateTime() {
        return responseDateTime;
    }

    public void setResponseDateTime(Date responseDateTime) {
        this.responseDateTime = responseDateTime;
    }
}
