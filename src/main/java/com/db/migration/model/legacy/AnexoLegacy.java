package com.db.migration.model.legacy;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnexoLegacy {

    private Long cdAnexo;
    private Long nrChamado;
    private String nmArquivo;
    private String dsCaminho;
    private Long nrTamanhoKb;
    private Instant dtUpload;

}
