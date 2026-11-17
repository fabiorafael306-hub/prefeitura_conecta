package com.prefeitura.conecta.config;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prefeitura.conecta.shared.security.UsuarioAutenticado;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import java.io.IOException;
import java.util.List;
import java.util.Map;
/**
 * Regras de acesso da API.
 * Público: cadastro, login e catálogo. Todo o resto exige "Authorization: Bearer <jwt>".
 * A posse de protocolos é verificada na camada de serviço (ProtocoloService), com base no usuário do token.
 */
@Configuration
public class SecurityConfig {
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}

 @Bean
 SecurityFilterChain filterChain(HttpSecurity http,ObjectMapper mapper) throws Exception {
  http
   .csrf(csrf->csrf.disable()) // API stateless com token no header (sem cookies de sessão) → CSRF não se aplica
   .cors(Customizer.withDefaults())
   .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .authorizeHttpRequests(auth->auth
    .requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
    .requestMatchers(HttpMethod.POST,"/api/usuarios","/api/auth/login").permitAll()
    .requestMatchers(HttpMethod.GET,"/api/catalogo/**").permitAll()
    .requestMatchers(HttpMethod.GET,"/api/usuarios").hasRole("ADMIN")
    .requestMatchers("/error").permitAll()
    .anyRequest().authenticated())
   .oauth2ResourceServer(o->o
    .jwt(j->j.jwtAuthenticationConverter(conversorDePapel()))
    .authenticationEntryPoint((req,res,ex)->escreverErro(res,mapper,HttpServletResponse.SC_UNAUTHORIZED,"Faça login para continuar."))
    .accessDeniedHandler((req,res,ex)->escreverErro(res,mapper,HttpServletResponse.SC_FORBIDDEN,"Você não tem permissão para acessar este recurso.")))
   .exceptionHandling(e->e
    .authenticationEntryPoint((req,res,ex)->escreverErro(res,mapper,HttpServletResponse.SC_UNAUTHORIZED,"Faça login para continuar."))
    .accessDeniedHandler((req,res,ex)->escreverErro(res,mapper,HttpServletResponse.SC_FORBIDDEN,"Você não tem permissão para acessar este recurso.")));
  return http.build();
 }

 /** Converte a claim "papel" (CIDADAO/ADMIN) em ROLE_CIDADAO/ROLE_ADMIN. */
 private JwtAuthenticationConverter conversorDePapel(){
  var conv=new JwtAuthenticationConverter();
  conv.setJwtGrantedAuthoritiesConverter(jwt->{
   String papel=jwt.getClaimAsString(UsuarioAutenticado.CLAIM_PAPEL);
   return papel==null?List.of():List.of(new SimpleGrantedAuthority("ROLE_"+papel));
  });
  return conv;
 }

 private static void escreverErro(HttpServletResponse res,ObjectMapper mapper,int status,String msg) throws IOException {
  res.setStatus(status);
  res.setContentType(MediaType.APPLICATION_JSON_VALUE);
  res.setCharacterEncoding("UTF-8");
  mapper.writeValue(res.getOutputStream(),Map.of("erro",msg));
 }
}
