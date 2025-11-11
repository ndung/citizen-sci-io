package io.sci.citizen.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TextQueryRequest {

    private Long id;                       // null for create
    @NotBlank private String attribute;
    @NotBlank private String question;
    @NotNull private Integer type;
    private boolean enabled;
    @NotNull private Integer sequence;
    private boolean required;
    private Long sectionId;              // can be null
    private List<QueryOptionRequest> options = new ArrayList<>();

}
