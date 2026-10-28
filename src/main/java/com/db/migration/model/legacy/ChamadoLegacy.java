package com.db.migration.model.legacy;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChamadoLegacy {

    private Long nrChamado;
    private String dsTitulo;
    private String dsChamado;
    private Long cdSolicitante;
    private Long cdTecnico;
    private Long cdCategoria;
    private Long idPrioridade;
    private String stChamado;
    private Instant dtAbertura;
    private String hrAbertura;
    private Instant dtFechadamento;
    private Long nrNotaAvaliacao;
    private String dsAvaliacao;
    private String flSlaEstourado;
    private String dsTags;

}
