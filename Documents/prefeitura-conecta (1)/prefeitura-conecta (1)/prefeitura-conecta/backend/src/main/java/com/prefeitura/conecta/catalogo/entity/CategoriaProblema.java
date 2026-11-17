package com.prefeitura.conecta.catalogo.entity;
import jakarta.persistence.*;
@Entity @Table(name="categorias_problema")
public class CategoriaProblema {
 @Id private Long id;
 @Column(name="codigo_area",nullable=false,length=50) private String codigoArea;
 @Column(name="nome_area",nullable=false,length=100) private String nomeArea;
 @Column(name="codigo_tipo",nullable=false,length=70) private String codigoTipo;
 @Column(name="nome_tipo",nullable=false,length=120) private String nomeTipo;
 @Column(nullable=false) private boolean ativo=true;
 protected CategoriaProblema(){}
 public Long getId(){return id;} public String getCodigoArea(){return codigoArea;} public String getNomeArea(){return nomeArea;}
 public String getCodigoTipo(){return codigoTipo;} public String getNomeTipo(){return nomeTipo;} public boolean isAtivo(){return ativo;}
}
