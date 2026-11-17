package com.prefeitura.conecta.protocolo.service;
import com.prefeitura.conecta.protocolo.dto.ProtocoloResponse;
import com.prefeitura.conecta.protocolo.entity.Protocolo;
import com.prefeitura.conecta.protocolo.repository.ProtocoloRepository;
import com.prefeitura.conecta.shared.security.UsuarioAutenticado;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.NoSuchElementException;
/**
 * Consulta de protocolos com isolamento por cidadão.
 * Protocolo de outro cidadão responde 404 (e não 403) para não revelar que o ID/código existe.
 */
@Service @Transactional(readOnly=true)
public class ProtocoloService {
 private final ProtocoloRepository repo;
 public ProtocoloService(ProtocoloRepository repo){this.repo=repo;}

 public ProtocoloResponse buscarPorId(Long id,UsuarioAutenticado solicitante){
  return ProtocoloResponse.from(verificarPosse(repo.buscarCompletoPorId(id).orElseThrow(this::naoEncontrado),solicitante));
 }
 public ProtocoloResponse buscarPorCodigo(String codigo,UsuarioAutenticado solicitante){
  String normalizado=codigo==null?"":codigo.trim().toUpperCase();
  return ProtocoloResponse.from(verificarPosse(repo.buscarCompletoPorCodigo(normalizado).orElseThrow(this::naoEncontrado),solicitante));
 }
 /** Mantido por compatibilidade com o contrato original: só aceita o próprio usuarioId (ou ADMIN). */
 public List<ProtocoloResponse> listarPorUsuario(Long usuarioId,UsuarioAutenticado solicitante){
  if(!solicitante.admin() && !solicitante.id().equals(usuarioId))
   throw new AccessDeniedException("Você só pode consultar os seus próprios protocolos.");
  return repo.listarPorUsuario(usuarioId).stream().map(ProtocoloResponse::from).toList();
 }
 public List<ProtocoloResponse> listarDoSolicitante(UsuarioAutenticado solicitante){
  return repo.listarPorUsuario(solicitante.id()).stream().map(ProtocoloResponse::from).toList();
 }

 private Protocolo verificarPosse(Protocolo p,UsuarioAutenticado solicitante){
  if(solicitante.admin()) return p;
  if(!p.getReclamacao().getUsuario().getId().equals(solicitante.id())) throw naoEncontrado();
  return p;
 }
 private NoSuchElementException naoEncontrado(){return new NoSuchElementException("Protocolo não encontrado.");}
}
