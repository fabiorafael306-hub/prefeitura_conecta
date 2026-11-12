package com.prefeitura.conecta.controller;

import com.prefeitura.conecta.dto.ReclamacaoDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/new_user")
public class ReclamacaoController {

    @PostMapping
    public ResponseEntity<Map<String, Object>> cadastrar(
            @Valid @RequestBody ReclamacaoDTO reclamacao) {

        Map<String, Object> resposta = Map.of(
                "mensagem", "Usuário cadastrado com sucesso!",
                "nome", reclamacao.getName(),
                "email", reclamacao.getEmail(),
                "categoria", reclamacao.getCategoria(),
                "tipo_usuario", reclamacao.getTipo_usuario()

        );

        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @GetMapping("/teste")
    public ResponseEntity<String> testarApi() {
        return ResponseEntity.ok("API Prefeitura Conecta funcionando!");
    }
}
