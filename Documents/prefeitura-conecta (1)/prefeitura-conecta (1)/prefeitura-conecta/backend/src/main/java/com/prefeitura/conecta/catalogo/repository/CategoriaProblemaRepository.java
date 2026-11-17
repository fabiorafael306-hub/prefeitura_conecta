package com.prefeitura.conecta.catalogo.repository;
import com.prefeitura.conecta.catalogo.entity.CategoriaProblema;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CategoriaProblemaRepository extends JpaRepository<CategoriaProblema,Long> {
 List<CategoriaProblema> findByAtivoTrueOrderByNomeAreaAscNomeTipoAsc();
}
