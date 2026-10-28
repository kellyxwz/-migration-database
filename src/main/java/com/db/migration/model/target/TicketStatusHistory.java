package com.db.migration.model.target;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketStatusHistory {

    private Long id;
    private UUID ticketId;
    private String fromStatus;
    private String toStatus;
    private UUID changedBy;
    private Instant changedAt;
    private Long legacyInteractionId;
}
