package com.prefeitura.conecta.reclamacao.controller;
import com.prefeitura.conecta.reclamacao.dto.ReclamacaoRequest;
import com.prefeitura.conecta.reclamacao.service.ReclamacaoService;
import com.prefeitura.conecta.protocolo.dto.ProtocoloResponse;
import com.prefeitura.conecta.shared.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/reclamacoes")
public class ReclamacaoController {
 private final ReclamacaoService service;
 public ReclamacaoController(ReclamacaoService service){this.service=service;}
 /** Registra a reclamação do cidadão autenticado e devolve o protocolo gerado (201). */
 @PostMapping @ResponseStatus(HttpStatus.CREATED)
 public ProtocoloResponse registrar(@Valid @RequestBody ReclamacaoRequest req,@AuthenticationPrincipal Jwt jwt){
  return service.registrar(req,UsuarioAutenticado.from(jwt).id());
 }
}
