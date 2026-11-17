package com.prefeitura.conecta.auth.controller;
import com.prefeitura.conecta.auth.dto.*;
import com.prefeitura.conecta.auth.service.AuthService;
import com.prefeitura.conecta.shared.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/auth")
public class AuthController {
 private final AuthService service;
 public AuthController(AuthService service){this.service=service;}
 /** Público: troca CPF/e-mail + senha por um JWT. */
 @PostMapping("/login") public LoginResponse login(@Valid @RequestBody LoginRequest req){return service.login(req);}
 /** Autenticado: dados do cidadão dono do token. */
 @GetMapping("/me") public UsuarioResponse me(@AuthenticationPrincipal Jwt jwt){return service.me(UsuarioAutenticado.from(jwt).id());}
}
