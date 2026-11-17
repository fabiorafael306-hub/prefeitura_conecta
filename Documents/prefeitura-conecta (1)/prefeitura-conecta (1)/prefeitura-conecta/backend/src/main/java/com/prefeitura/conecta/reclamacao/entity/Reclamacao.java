package com.prefeitura.conecta.reclamacao.entity;
import com.prefeitura.conecta.auth.entity.Usuario;
import com.prefeitura.conecta.catalogo.entity.CategoriaProblema;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="reclamacoes")
public class Reclamacao {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="usuario_id",nullable=false) private Usuario usuario;
 @ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="categoria_id",nullable=false) private CategoriaProblema categoria;
 @Column(nullable=false,columnDefinition="text") private String descricao;
 @Column(nullable=false,length=250) private String endereco;
 @Column(nullable=false) private LocalDateTime criadaEm=LocalDateTime.now();
 protected Reclamacao(){}
 public Reclamacao(Usuario u,CategoriaProblema c,String d,String e){usuario=u;categoria=c;descricao=d;endereco=e;}
 public Long getId(){return id;} public Usuario getUsuario(){return usuario;} public CategoriaProblema getCategoria(){return categoria;}
 public String getDescricao(){return descricao;} public String getEndereco(){return endereco;} public LocalDateTime getCriadaEm(){return criadaEm;}
}
