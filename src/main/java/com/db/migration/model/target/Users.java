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
public class Users {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Long departmentId;
    private String role;
    private String status;
    private String passwordHash;
    private boolean mustResetPassword;
    private Instant createdAt;
    private Instant updatedAt;
    private Long legacyId;


}
