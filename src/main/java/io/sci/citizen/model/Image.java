package io.sci.citizen.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name="image")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uuid;

    private String originalFileName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "data_id")
    @NotFound(action = NotFoundAction.IGNORE)
    @JsonIgnore
    private Data data;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private Section section;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Integer status;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getOriginalFileName() { return originalFileName; }

    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Image image = (Image) o;
        return Objects.equals(id, image.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Image{" +
                "id=" + id +
                ", data.Uuid='" + data.getUuid() + '\'' +
                ", originalFileName='" + originalFileName + '\'' +
                '}';
    }
}
