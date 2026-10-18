package com.db.migration.model.legacy;

import java.time.Instant;

public record UserActivityDateLegacy(
        Long userId,
        Instant activityAt
) {
}
