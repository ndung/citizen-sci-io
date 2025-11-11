package io.sci.citizen.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueryOptionRequest {

    private Long id;                // null for new
    private Integer sequence;
    private boolean enabled;
    private String description;

    @Override
    public String toString() {
        return "QueryOptionRequest{" +
                "id=" + id +
                ", sequence=" + sequence +
                ", enabled=" + enabled +
                ", description='" + description + '\'' +
                '}';
    }
}
