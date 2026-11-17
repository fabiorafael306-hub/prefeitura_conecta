package com.prefeitura.conecta.catalogo.controller;
import com.prefeitura.conecta.catalogo.dto.CategoriaResponse;
import com.prefeitura.conecta.catalogo.repository.CategoriaProblemaRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/catalogo")
public class CatalogoController {
 private final CategoriaProblemaRepository repo;
 public CatalogoController(CategoriaProblemaRepository repo){this.repo=repo;}
 @GetMapping("/categorias") public List<CategoriaResponse> categorias(){return repo.findByAtivoTrueOrderByNomeAreaAscNomeTipoAsc().stream().map(CategoriaResponse::from).toList();}
}
