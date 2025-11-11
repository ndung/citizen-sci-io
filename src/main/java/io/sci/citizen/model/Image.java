package io.sci.citizen.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.time.Instant;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="image")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Image {

    @EqualsAndHashCode.Include
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

    @Transient
    public String getUrl(){
        return "https://citizen-sci-io-c296af702ec9.herokuapp.com/files/"+uuid;
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
