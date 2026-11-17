package com.prefeitura.conecta.auth.service;
import com.prefeitura.conecta.auth.dto.*;
import com.prefeitura.conecta.auth.entity.Usuario;
import com.prefeitura.conecta.auth.repository.UsuarioRepository;
import com.prefeitura.conecta.shared.exception.CredenciaisInvalidasException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;
import java.util.Optional;
@Service
public class AuthService {
 private final UsuarioRepository usuarios; private final PasswordEncoder passwordEncoder; private final TokenService tokens;
 /** Hash usado quando o login não existe, para que o tempo de resposta não revele quais CPFs/e-mails estão cadastrados. */
 private final String hashFicticio;
 public AuthService(UsuarioRepository u,PasswordEncoder p,TokenService t){usuarios=u;passwordEncoder=p;tokens=t;hashFicticio=p.encode("senha-inexistente-para-comparacao");}

 @Transactional(readOnly=true)
 public LoginResponse login(LoginRequest req){
  Optional<Usuario> encontrado=buscarPorLogin(req.login());
  String hash=encontrado.map(Usuario::getSenhaHash).orElse(null);
  boolean senhaOk=passwordEncoder.matches(req.senha(),hash!=null?hash:hashFicticio);
  if(encontrado.isEmpty()||hash==null||!senhaOk) throw new CredenciaisInvalidasException();
  Usuario u=encontrado.get();
  var token=tokens.emitir(u);
  return new LoginResponse(token.valor(),"Bearer",token.expiraEm(),UsuarioResponse.from(u));
 }

 @Transactional(readOnly=true)
 public UsuarioResponse me(Long usuarioId){
  return UsuarioResponse.from(usuarios.findById(usuarioId).orElseThrow(()->new java.util.NoSuchElementException("Cidadão não encontrado.")));
 }

 private Optional<Usuario> buscarPorLogin(String login){
  String l=login.trim();
  String digitos=l.replaceAll("[.\\-\\s]","");
  if(digitos.matches("[0-9]{11}")) return usuarios.findByCpf(digitos);
  return usuarios.findByEmailIgnoreCase(l.toLowerCase(Locale.ROOT));
 }
}
