package com.db.migration.model.legacy;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioLegacy {

    private Long cdUsuario;
    private String name;
    private String dsEmail;
    private String nrTelefone;
    private Long cdSetor;
    private String tpPerfil;
    private String dsSenha;
    private String dtCadastro;
    private String flAtivo;


}
