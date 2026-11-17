package com.prefeitura.conecta.auth.controller;
import com.prefeitura.conecta.auth.dto.*;
import com.prefeitura.conecta.auth.service.UsuarioService;
import com.prefeitura.conecta.shared.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/usuarios")
public class UsuarioController {
 private final UsuarioService service;
 public UsuarioController(UsuarioService service){this.service=service;}
 /** Público: cadastro do cidadão. */
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public UsuarioResponse criar(@Valid @RequestBody UsuarioRequest r){return service.criar(r);}
 /** Somente ADMIN (ver SecurityConfig). */
 @GetMapping public List<UsuarioResponse> listar(){return service.listar();}
 /** Autenticado: o próprio cidadão (ou ADMIN). */
 @GetMapping("/{id}") public UsuarioResponse buscar(@PathVariable Long id,@AuthenticationPrincipal Jwt jwt){return service.buscar(id,UsuarioAutenticado.from(jwt));}
}
