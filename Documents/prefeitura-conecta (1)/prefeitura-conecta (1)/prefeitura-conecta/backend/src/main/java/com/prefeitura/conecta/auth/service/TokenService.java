package com.prefeitura.conecta.auth.service;
import com.prefeitura.conecta.auth.entity.Usuario;
import com.prefeitura.conecta.shared.security.UsuarioAutenticado;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
/** Emite o JWT de acesso do cidadão. Assinatura HS256 com o segredo app.jwt.secret. */
@Service
public class TokenService {
 public static final String ISSUER="prefeitura-conecta";
 private final JwtEncoder encoder; private final long expiracaoMinutos;
 public TokenService(JwtEncoder encoder,@Value("${app.jwt.expiracao-minutos:60}") long expiracaoMinutos){this.encoder=encoder;this.expiracaoMinutos=expiracaoMinutos;}
 public record TokenEmitido(String valor,Instant expiraEm) {}
 public TokenEmitido emitir(Usuario u){
  Instant agora=Instant.now(); Instant exp=agora.plus(expiracaoMinutos,ChronoUnit.MINUTES);
  // Apenas id e papel no token: o JWT é legível por qualquer um (só é assinado, não criptografado). Não colocar CPF/e-mail.
  var claims=JwtClaimsSet.builder().issuer(ISSUER).issuedAt(agora).expiresAt(exp)
   .subject(String.valueOf(u.getId())).claim(UsuarioAutenticado.CLAIM_PAPEL,u.getTipoUsuario()).build();
  var header=JwsHeader.with(MacAlgorithm.HS256).build();
  return new TokenEmitido(encoder.encode(JwtEncoderParameters.from(header,claims)).getTokenValue(),exp);
 }
}
