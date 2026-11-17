package com.prefeitura.conecta.auth.dto;
import com.prefeitura.conecta.auth.entity.Usuario;
public record UsuarioResponse(Long id,String nome,String email,String cpf,String endereco,String tipoUsuario) {
 public static UsuarioResponse from(Usuario u){return new UsuarioResponse(u.getId(),u.getNome(),u.getEmail(),u.getCpf(),u.getEndereco(),u.getTipoUsuario());}
}
