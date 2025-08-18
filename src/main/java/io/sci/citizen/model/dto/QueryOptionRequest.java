package io.sci.citizen.model.dto;

public class QueryOptionRequest {

    private Long id;                // null for new
    private Integer sequence;
    private boolean enabled;
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

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
