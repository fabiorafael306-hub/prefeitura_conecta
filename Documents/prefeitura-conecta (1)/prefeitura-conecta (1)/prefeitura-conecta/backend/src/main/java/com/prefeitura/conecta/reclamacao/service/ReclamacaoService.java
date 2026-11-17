package com.prefeitura.conecta.reclamacao.service;
import com.prefeitura.conecta.auth.repository.UsuarioRepository;
import com.prefeitura.conecta.catalogo.repository.CategoriaProblemaRepository;
import com.prefeitura.conecta.protocolo.entity.Protocolo;
import com.prefeitura.conecta.protocolo.dto.ProtocoloResponse;
import com.prefeitura.conecta.protocolo.repository.ProtocoloRepository;
import com.prefeitura.conecta.reclamacao.dto.ReclamacaoRequest;
import com.prefeitura.conecta.reclamacao.entity.Reclamacao;
import com.prefeitura.conecta.reclamacao.repository.ReclamacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
@Service
public class ReclamacaoService {
 private final UsuarioRepository usuarios; private final CategoriaProblemaRepository categorias;
 private final ReclamacaoRepository reclamacoes; private final ProtocoloRepository protocolos;
 public ReclamacaoService(UsuarioRepository u,CategoriaProblemaRepository c,ReclamacaoRepository r,ProtocoloRepository p){usuarios=u;categorias=c;reclamacoes=r;protocolos=p;}
 /** @param usuarioId id do cidadão autenticado (vindo do JWT, nunca do corpo da requisição). */
 @Transactional public ProtocoloResponse registrar(ReclamacaoRequest req,Long usuarioId) {
  var usuario=usuarios.findById(usuarioId).orElseThrow(()->new java.util.NoSuchElementException("Cidadão não encontrado."));
  var categoria=categorias.findById(req.categoriaId()).filter(x->x.isAtivo()).orElseThrow(()->new IllegalArgumentException("Categoria inválida ou inativa."));
  var reclamacao=reclamacoes.save(new Reclamacao(usuario,categoria,req.descricao().trim(),req.endereco().trim()));
  String codigo="PC-"+LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)+"-"+UUID.randomUUID().toString().substring(0,8).toUpperCase();
  var protocolo=protocolos.save(new Protocolo(codigo,reclamacao));
  return ProtocoloResponse.from(protocolos.buscarCompletoPorId(protocolo.getId()).orElseThrow());
 }
}
