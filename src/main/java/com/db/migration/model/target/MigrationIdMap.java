package com.db.migration.model.target;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MigrationIdMap {

    private String entity;
    private Long legacyId;
    private String newId;

}
