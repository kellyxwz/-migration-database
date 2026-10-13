package com.db.migration.model.target;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Users {

    private long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Integer departmentId;
    private String role;
    private String passwordHash;
    private boolean musResetPassword;
    private Instant createdAt;
    private Instant updateAt;
    private Integer legacyId;


}
