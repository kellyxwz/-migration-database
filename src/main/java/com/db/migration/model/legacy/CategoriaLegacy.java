package com.db.migration.model.legacy;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaLegacy {

    private Long cdCategoria;
    private String dcCategoria;
    private Long cdCategoriaPai;
    private String flAtivo;

}
