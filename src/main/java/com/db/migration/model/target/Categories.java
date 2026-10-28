package com.db.migration.model.target;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Categories {

    private Long id;
    private String name;
    private Long parentId;
    private Long resolutionSlaMinutes;
    private boolean isActive;
    private Long legacyId;

}
