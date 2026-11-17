package com.prefeitura.conecta.auth.dto;
import jakarta.validation.constraints.NotBlank;
/** Login com CPF (11 dígitos) ou e-mail. */
public record LoginRequest(@NotBlank(message="Informe o CPF ou e-mail.") String login,
                           @NotBlank(message="Informe a senha.") String senha) {
 @Override public String toString(){return "LoginRequest[login="+login+", senha=***]";}
}
