package com.prefeitura.conecta.auth.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="usuarios")
public class Usuario {
 public static final String TIPO_CIDADAO="CIDADAO";
 public static final String TIPO_ADMIN="ADMIN";

 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=150) private String nome;
 @Column(nullable=false,length=180,unique=true) private String email;
 @Column(nullable=false,length=11,unique=true) private String cpf;
 @Column(length=250) private String endereco;
 @Column(nullable=false,length=20) private String tipoUsuario=TIPO_CIDADAO;
 /** Hash BCrypt da senha. Nunca armazena a senha em texto puro. Nulo apenas para cadastros anteriores à autenticação. */
 @Column(name="senha_hash",length=100) private String senhaHash;
 @Column(nullable=false) private LocalDateTime criadoEm=LocalDateTime.now();
 protected Usuario() {}
 public Usuario(String nome,String email,String cpf,String endereco,String senhaHash) {
  this.nome=nome;this.email=email;this.cpf=cpf;this.endereco=endereco;this.senhaHash=senhaHash;
 }
 public Long getId(){return id;} public String getNome(){return nome;} public String getEmail(){return email;}
 public String getCpf(){return cpf;} public String getEndereco(){return endereco;} public String getTipoUsuario(){return tipoUsuario;}
 public String getSenhaHash(){return senhaHash;}
 public LocalDateTime getCriadoEm(){return criadoEm;}
 public boolean isAdmin(){return TIPO_ADMIN.equals(tipoUsuario);}
}
