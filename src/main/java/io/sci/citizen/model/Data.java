package io.sci.citizen.model;

import jakarta.persistence.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name="data")
public class Data implements Serializable {

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAccuracy() { return accuracy; }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }

    public User getUser() { return user; }

    public void setUser(User userId) { this.user = userId; }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setSurveyResponses(List<QueryReply> surveyResponses) {
        this.surveyResponses = surveyResponses;
    }

    public int getStatus() { return status; }

    public void setStatus(int status) { this.status = status; }

    public List<Image> getImages() { return images; }

    public void setImages(List<Image> images) { this.images = images; }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public List<QueryReply> getSurveyResponses() {
        return surveyResponses;
    }

    public User getVerificator() {
        return verificator;
    }

    public void setVerificator(User verificator) {
        this.verificator = verificator;
    }

    public Date getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Date verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    @Transient
    public String getDetails(){
        StringBuilder sb = new StringBuilder();
        for (QueryReply reply : surveyResponses) {
            sb.append("{\"").append(reply.getQuestion().getAttribute()).append("\" : ");
            if (!reply.getResponse().startsWith("[")){
                sb.append("\"").append(reply.getResponse()).append("\"");
            }else {
                sb.append(reply.getResponse()).append(", ");
            }
        }
        String details = sb.toString();
        return details.substring(0, details.length()-2)+"}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Data data = (Data) o;
        return Objects.equals(id, data.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
