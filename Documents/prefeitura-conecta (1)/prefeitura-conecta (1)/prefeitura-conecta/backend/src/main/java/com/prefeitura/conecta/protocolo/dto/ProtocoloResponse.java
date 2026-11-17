package com.prefeitura.conecta.protocolo.dto;
import com.prefeitura.conecta.protocolo.entity.*;
import java.time.LocalDateTime;
public record ProtocoloResponse(Long id,String codigo,String status,LocalDateTime criadoEm,
 Long reclamacaoId,String nomeSolicitante,String categoria,String tipoProblema,String descricao,String endereco) {
 public static ProtocoloResponse from(Protocolo p) {
  var r=p.getReclamacao(); var c=r.getCategoria();
  return new ProtocoloResponse(p.getId(),p.getCodigo(),p.getStatus().name(),p.getCriadoEm(),
   r.getId(),r.getUsuario().getNome(),c.getNomeArea(),c.getNomeTipo(),r.getDescricao(),r.getEndereco());
 }
}
