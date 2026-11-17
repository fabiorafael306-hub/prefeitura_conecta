package com.prefeitura.conecta.protocolo.entity;
import com.prefeitura.conecta.reclamacao.entity.Reclamacao;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="protocolos")
public class Protocolo {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true,length=40) private String codigo;
 @OneToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="reclamacao_id",nullable=false,unique=true) private Reclamacao reclamacao;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private StatusProtocolo status=StatusProtocolo.EM_ANALISE;
 @Column(name="criado_em",nullable=false) private LocalDateTime criadoEm=LocalDateTime.now();
 protected Protocolo(){}
 public Protocolo(String codigo,Reclamacao r){this.codigo=codigo;this.reclamacao=r;this.status=StatusProtocolo.EM_ANALISE;}
 public Long getId(){return id;} public String getCodigo(){return codigo;} public Reclamacao getReclamacao(){return reclamacao;}
 public StatusProtocolo getStatus(){return status;} public LocalDateTime getCriadoEm(){return criadoEm;}
}
