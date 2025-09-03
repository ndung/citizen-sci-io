package io.sci.citizen.api.dto;

import java.util.Date;

public record RecordData(
        String uuid,
        double latitude,
        double longitude,
        double accuracy,
        int status,
        Long projectId,
        Long userId,
        Date startDate,
        Date finishDate
) {}