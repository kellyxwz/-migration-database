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
public class InteracaoLegacy {
    
  private Long cdInteracao;
  private Long nrChamado;
  private Long cd_usuario;
  private String dsInteracao;
  private Instant dtInteracao;
  private String flIntercao;
  
}