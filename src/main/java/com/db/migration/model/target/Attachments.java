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
public class Attachments {

    private UUID id;
    private UUID ticketId;
    private String fileNme;
    private String storageKey;
    private String mimeType;
    private Long sizeBytes;
    private Instant uploadedAt;
    private Long legacyId;

}
