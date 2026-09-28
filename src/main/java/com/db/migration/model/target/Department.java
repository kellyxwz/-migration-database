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
public class Department {

    private Long id;
    private String name;
    private boolean isActive;
    private Integer legacyId;
    private Instant  createdAt;

    public Department(String name, boolean isActive, Integer legacyId, Instant createdAt) {
        this.name = name;
        this.isActive = isActive;
        this.legacyId = legacyId;
        this.createdAt = createdAt;
    }
}
