package com.db.migration.model.target;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MigrationIssues {

    private Long id;
    private String entity;
    private Long legacyId;
    private String severite;
    private String code;
    private String message;
    private Instant createdAt;

}
