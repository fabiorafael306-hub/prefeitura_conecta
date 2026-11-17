package com.prefeitura.conecta.protocolo.repository;
import com.prefeitura.conecta.protocolo.entity.Protocolo;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface ProtocoloRepository extends JpaRepository<Protocolo,Long> {
 @Query("select p from Protocolo p join fetch p.reclamacao r join fetch r.usuario join fetch r.categoria where p.id=:id")
 Optional<Protocolo> buscarCompletoPorId(@Param("id") Long id);
 @Query("select p from Protocolo p join fetch p.reclamacao r join fetch r.usuario join fetch r.categoria where p.codigo=:codigo")
 Optional<Protocolo> buscarCompletoPorCodigo(@Param("codigo") String codigo);
 @Query("select p from Protocolo p join fetch p.reclamacao r join fetch r.usuario join fetch r.categoria where r.usuario.id=:usuarioId order by p.criadoEm desc")
 List<Protocolo> listarPorUsuario(@Param("usuarioId") Long usuarioId);
}
