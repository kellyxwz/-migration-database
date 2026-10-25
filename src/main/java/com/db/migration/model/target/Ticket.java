package com.db.migration.model.target;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Ticket {

    private UUID id;
    private String code;
    private String title;
    private String description;
    private UUID requesterId;
    private UUID assigneeId;
    private Long  categoryId;
    private String priority;
    private String status;
    private Instant openedAt;
    private Instant firstResponseAt;
    private Instant resolvedAt;
    private Instant closedAt;
    private Instant dueAt;
    private Boolean slaBreached;
    private Long satisfactionScore;
    private String satisfactionComment;
    private Instant createdAt;
    private Instant updatedAt;
    private Long legacyId;

}
