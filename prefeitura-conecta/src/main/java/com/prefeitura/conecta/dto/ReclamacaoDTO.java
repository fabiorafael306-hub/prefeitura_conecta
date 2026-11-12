package com.prefeitura.conecta.dto;

import jakarta.validation.constraints.NotBlank;

public class ReclamacaoDTO {

    @NotBlank(message = "O Nome é obrigatório.")
    private String name;

    @NotBlank(message = "O E-mail é obrigatória.")
    private String email;

    @NotBlank(message = "O endereço é obrigatório.")
    private String endereco;

    @NotBlank(message = "A categoria é obrigatória.")
    private String categoria;

    @NotBlank(message = "O tipo de usuário é obrigatória.")
    private String tipo_usuario;

    @NotBlank(message = "O CPF é obrigatória.")
    private String cpf;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getTipo_usuario() {
        return tipo_usuario;
    }

    public void setTipo_usuario(String tipo_usuario) {
        this.tipo_usuario = tipo_usuario;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
}
