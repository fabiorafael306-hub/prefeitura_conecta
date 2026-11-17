package com.prefeitura.conecta.auth.dto;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;
/** Dados de cadastro do cidadão. A senha é recebida apenas para gerar o hash BCrypt. */
public record UsuarioRequest(
 @NotBlank(message="Informe o nome.") @Size(max=150,message="O nome deve ter no máximo 150 caracteres.") String nome,
 @NotBlank(message="Informe o e-mail.") @Email(message="E-mail inválido.") @Size(max=180,message="O e-mail deve ter no máximo 180 caracteres.") String email,
 @NotBlank(message="Informe o CPF.") @Pattern(regexp="[0-9]{11}",message="CPF deve conter 11 números") @CPF(message="CPF inválido.") String cpf,
 @Size(max=250,message="O endereço deve ter no máximo 250 caracteres.") String endereco,
 // BCrypt considera no máximo 72 bytes; limitamos a 72 caracteres.
 @NotBlank(message="Informe a senha.") @Size(min=8,max=72,message="A senha deve ter entre 8 e 72 caracteres.") String senha) {
 @Override public String toString(){return "UsuarioRequest[nome="+nome+", email="+email+", senha=***]";}
}
