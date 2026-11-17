package com.prefeitura.conecta.reclamacao.dto;
import jakarta.validation.constraints.*;
/**
 * Registro de reclamação. O cidadão NÃO é informado aqui: ele é obtido do token JWT.
 * (O campo usuarioId foi removido; se um cliente antigo enviá-lo, ele é ignorado.)
 */
public record ReclamacaoRequest(
 @NotNull(message="Selecione o tipo de problema.") @Positive(message="Categoria inválida.") Long categoriaId,
 @NotBlank(message="Descreva o problema.") @Size(max=5000,message="A descrição deve ter no máximo 5000 caracteres.") String descricao,
 @NotBlank(message="Informe o endereço da ocorrência.") @Size(max=250,message="O endereço deve ter no máximo 250 caracteres.") String endereco) {}
