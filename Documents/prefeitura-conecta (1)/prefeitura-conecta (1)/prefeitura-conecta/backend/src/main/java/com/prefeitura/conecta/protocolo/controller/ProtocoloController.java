package com.prefeitura.conecta.protocolo.controller;
import com.prefeitura.conecta.protocolo.dto.ProtocoloResponse;
import com.prefeitura.conecta.protocolo.service.ProtocoloService;
import com.prefeitura.conecta.shared.security.UsuarioAutenticado;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.List;
/** Todos os endpoints exigem JWT. Somente leitura: o cidadão não altera status. */
@RestController @RequestMapping("/api/protocolos")
public class ProtocoloController {
 private final ProtocoloService service;
 public ProtocoloController(ProtocoloService service){this.service=service;}
 /** Atalho recomendado para a aba "Meus protocolos": usa o cidadão do token. */
 @GetMapping("/meus") public List<ProtocoloResponse> meus(@AuthenticationPrincipal Jwt jwt){return service.listarDoSolicitante(UsuarioAutenticado.from(jwt));}
 @GetMapping("/{id}") public ProtocoloResponse porId(@PathVariable Long id,@AuthenticationPrincipal Jwt jwt){return service.buscarPorId(id,UsuarioAutenticado.from(jwt));}
 @GetMapping("/codigo/{codigo}") public ProtocoloResponse porCodigo(@PathVariable String codigo,@AuthenticationPrincipal Jwt jwt){return service.buscarPorCodigo(codigo,UsuarioAutenticado.from(jwt));}
 @GetMapping("/usuario/{usuarioId}") public List<ProtocoloResponse> porUsuario(@PathVariable Long usuarioId,@AuthenticationPrincipal Jwt jwt){return service.listarPorUsuario(usuarioId,UsuarioAutenticado.from(jwt));}
}
