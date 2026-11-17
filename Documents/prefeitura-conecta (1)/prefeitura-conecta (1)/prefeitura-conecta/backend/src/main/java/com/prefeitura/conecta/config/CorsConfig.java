package com.prefeitura.conecta.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.List;
/**
 * CORS integrado ao Spring Security (o antigo WebMvcConfigurer não é aplicado antes dos filtros de segurança,
 * o que faria o preflight OPTIONS com header Authorization ser bloqueado com 401).
 * Origens configuráveis por app.cors.allowed-origins (padrão: localhost:5173 e localhost:3000, como antes).
 */
@Configuration
public class CorsConfig {
 @Bean
 CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins}") String origens) {
  var config=new CorsConfiguration();
  config.setAllowedOrigins(Arrays.stream(origens.split(",")).map(String::trim).filter(s->!s.isEmpty()).toList());
  config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
  config.setAllowedHeaders(List.of("Authorization","Content-Type"));
  config.setMaxAge(3600L);
  var source=new UrlBasedCorsConfigurationSource();
  source.registerCorsConfiguration("/api/**",config);
  return source;
 }
}
