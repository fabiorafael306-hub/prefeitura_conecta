package com.prefeitura.conecta.auth.service;
import com.prefeitura.conecta.auth.dto.*;
import com.prefeitura.conecta.auth.entity.Usuario;
import com.prefeitura.conecta.auth.repository.UsuarioRepository;
import com.prefeitura.conecta.shared.exception.ConflitoException;
import com.prefeitura.conecta.shared.security.UsuarioAutenticado;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;
@Service
public class UsuarioService {
 private final UsuarioRepository repo;
 private final PasswordEncoder passwordEncoder;
 public UsuarioService(UsuarioRepository repo,PasswordEncoder passwordEncoder){this.repo=repo;this.passwordEncoder=passwordEncoder;}

 @Transactional public UsuarioResponse criar(UsuarioRequest r){
  String email=r.email().trim().toLowerCase(Locale.ROOT);
  if(repo.existsByCpf(r.cpf())) throw new ConflitoException("CPF já cadastrado.");
  if(repo.existsByEmailIgnoreCase(email)) throw new ConflitoException("E-mail já cadastrado.");
  String endereco=r.endereco()==null||r.endereco().isBlank()?null:r.endereco().trim();
  var usuario=new Usuario(r.nome().trim(),email,r.cpf(),endereco,passwordEncoder.encode(r.senha()));
  return UsuarioResponse.from(repo.save(usuario));
 }

 /** Restrito a ADMIN pela SecurityConfig: expõe dados pessoais de todos os cidadãos. */
 public List<UsuarioResponse> listar(){return repo.findAll().stream().map(UsuarioResponse::from).toList();}

 /** O cidadão só consulta o próprio cadastro; ADMIN consulta qualquer um. */
 public UsuarioResponse buscar(Long id,UsuarioAutenticado solicitante){
  if(!solicitante.admin() && !solicitante.id().equals(id)) throw new AccessDeniedException("Você não tem permissão para consultar este cadastro.");
  return UsuarioResponse.from(repo.findById(id).orElseThrow(()->new java.util.NoSuchElementException("Cidadão não encontrado.")));
 }
}
