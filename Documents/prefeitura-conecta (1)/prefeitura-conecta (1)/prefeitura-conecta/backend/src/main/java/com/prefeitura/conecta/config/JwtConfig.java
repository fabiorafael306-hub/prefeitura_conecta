package com.prefeitura.conecta.config;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.prefeitura.conecta.auth.service.TokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
/** Chave HS256 e encoder/decoder de JWT. O segredo vem de JWT_SECRET e nunca é versionado. */
@Configuration
public class JwtConfig {
 private final SecretKey chave;
 public JwtConfig(@Value("${app.jwt.secret:}") String segredo){
  if(segredo==null||segredo.getBytes(StandardCharsets.UTF_8).length<32)
   throw new IllegalStateException("Defina JWT_SECRET com pelo menos 32 caracteres (ex.: openssl rand -base64 48). Veja .env.example.");
  this.chave=new SecretKeySpec(segredo.getBytes(StandardCharsets.UTF_8),"HmacSHA256");
 }
 @Bean JwtEncoder jwtEncoder(){return new NimbusJwtEncoder(new ImmutableSecret<>(chave));}
 @Bean JwtDecoder jwtDecoder(){
  NimbusJwtDecoder decoder=NimbusJwtDecoder.withSecretKey(chave).macAlgorithm(MacAlgorithm.HS256).build();
  // Valida expiração (padrão) e emissor.
  decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(TokenService.ISSUER)));
  return decoder;
 }
}
