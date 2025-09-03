package io.sci.citizen.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.time.Instant;
@Entity
@Table(name="image")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uuid;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "data_id")
    @NotFound(action = NotFoundAction.IGNORE)
    @JsonIgnore
    private Data data;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "section_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private Section section;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Integer status;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getUuid() { return uuid; }

    public void setUuid(String uuid) { this.uuid = uuid; }

    public Data getData() { return data; }

    public void setData(Data data) { this.data = data; }

    public Section getSection() { return section; }

    public void setSection(Section section) { this.section = section; }

    public Integer getStatus() { return status; }

    public void setStatus(Integer status) { this.status = status; }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Transient
    public String getUrl(){
        return "https://citizen-sci-io-c296af702ec9.herokuapp.com/files/"+uuid;
    }
}
