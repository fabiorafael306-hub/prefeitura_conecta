package com.prefeitura.conecta.catalogo.dto;
import com.prefeitura.conecta.catalogo.entity.CategoriaProblema;
public record CategoriaResponse(Long id,String codigoArea,String nomeArea,String codigoTipo,String nomeTipo) {
 public static CategoriaResponse from(CategoriaProblema c){return new CategoriaResponse(c.getId(),c.getCodigoArea(),c.getNomeArea(),c.getCodigoTipo(),c.getNomeTipo());}
}
