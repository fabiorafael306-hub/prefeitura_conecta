package com.prefeitura.conecta.shared.security;
import org.springframework.security.oauth2.jwt.Jwt;
/**
 * Identidade do cidadão extraída do JWT validado pelo Spring Security.
 * É a ÚNICA fonte confiável do usuário logado: nunca use um usuarioId vindo do corpo/URL da requisição para decidir acesso.
 */
public record UsuarioAutenticado(Long id,String papel) {
 public static final String CLAIM_PAPEL="papel";
 public static UsuarioAutenticado from(Jwt jwt){
  if(jwt==null) throw new IllegalStateException("Requisição sem autenticação.");
  return new UsuarioAutenticado(Long.valueOf(jwt.getSubject()),jwt.getClaimAsString(CLAIM_PAPEL));
 }
 public boolean admin(){return "ADMIN".equals(papel);}
}
