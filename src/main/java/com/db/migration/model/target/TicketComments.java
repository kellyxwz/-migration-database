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
public class TicketComments {

    private Long id;
    private UUID ticketId;
    private UUID authorId;
    private String body;
    private String visibility;
    private Instant createdAt;
    private Long legacyId;

}
