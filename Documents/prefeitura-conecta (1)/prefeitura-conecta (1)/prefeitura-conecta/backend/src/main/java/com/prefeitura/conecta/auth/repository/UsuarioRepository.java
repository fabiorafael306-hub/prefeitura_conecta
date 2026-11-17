package com.prefeitura.conecta.auth.repository;
import com.prefeitura.conecta.auth.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface UsuarioRepository extends JpaRepository<Usuario,Long> {
 boolean existsByEmailIgnoreCase(String email); boolean existsByCpf(String cpf);
 Optional<Usuario> findByCpf(String cpf);
 Optional<Usuario> findByEmailIgnoreCase(String email);
}
