package io.sci.citizen.api.dto;

import io.sci.citizen.model.Project;
import io.sci.citizen.model.User;

import java.util.Map;

public record LoginDetails(User user, Map<Long,Project> projects) {}
