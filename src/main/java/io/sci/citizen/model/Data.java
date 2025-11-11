package io.sci.citizen.model;

import jakarta.persistence.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="data")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Data implements Serializable {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String uuid;
    private double latitude;
    private double longitude;
    private double accuracy;
    private int status;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "project_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private Project project;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private User user;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "finish_date")
    private Date finishDate;

    @Column(nullable = false, updatable = false)
    private Date createdAt = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy="data", cascade = {CascadeType.ALL})
    private List<Image> images;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy="data", cascade = {CascadeType.ALL})
    private List<QueryReply> surveyResponses;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "verificator_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private User verificator;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "verified_at")
    private Date verifiedAt;

    @Transient
    public String getDetails(){
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (QueryReply reply : surveyResponses) {
            sb.append("\"").append(reply.getQuestion().getAttribute()).append("\" : ");
            if (!reply.getResponse().startsWith("[")){
                sb.append("\"").append(reply.getResponse()).append("\"");
            }else {
                sb.append(reply.getResponse());
            }
            sb.append(", ");
        }
        String details = sb.toString();
        return details.substring(0, details.length()-2)+"}";
    }
}
