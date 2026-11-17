package com.prefeitura.conecta;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prefeitura.conecta.auth.repository.UsuarioRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Fluxo completo do cidadão + isolamento entre cidadãos.
 * CPFs usados aqui são números de teste com dígitos verificadores válidos, não pertencem a cidadãos reais do sistema.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FluxoCidadaoIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UsuarioRepository usuarios;

    static String tokenAna, tokenBeto;
    static long idAna, idBeto, protocoloAnaId;
    static String protocoloAnaCodigo;

    private String cadastro(String nome, String email, String cpf, String senha) {
        return """
            {"nome":"%s","email":"%s","cpf":"%s","endereco":"Rua de Teste, 1","senha":"%s"}
            """.formatted(nome, email, cpf, senha);
    }

    private JsonNode body(org.springframework.test.web.servlet.ResultActions r) throws Exception {
        return json.readTree(r.andReturn().getResponse().getContentAsString());
    }

    private String login(String login, String senha) throws Exception {
        var r = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"%s\",\"senha\":\"%s\"}".formatted(login, senha)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tipo").value("Bearer"));
        return body(r).get("token").asText();
    }

    @Test @Order(1)
    void catalogoEhPublicoESoTrazAtivas() throws Exception {
        mvc.perform(get("/api/catalogo/categorias"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[*].id", not(hasItem(99))));
    }

    @Test @Order(2)
    void cadastroValidaCamposEGuardaSenhaComoHash() throws Exception {
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                .content(cadastro("", "invalido", "12345678900", "123")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.erro").value("Dados inválidos"))
            .andExpect(jsonPath("$.detalhes[*].campo", hasItems("nome", "email", "cpf", "senha")));

        var r = mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                .content(cadastro("Ana Teste", "Ana@Teste.com", "52998224725", "senhaDaAna1")))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("ana@teste.com"))
            .andExpect(jsonPath("$.senha").doesNotExist())
            .andExpect(jsonPath("$.senhaHash").doesNotExist());
        idAna = body(r).get("id").asLong();

        var hash = usuarios.findById(idAna).orElseThrow().getSenhaHash();
        assertThat(hash).startsWith("$2").doesNotContain("senhaDaAna1");

        var r2 = mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                .content(cadastro("Beto Teste", "beto@teste.com", "11144477735", "senhaDoBeto1")))
            .andExpect(status().isCreated());
        idBeto = body(r2).get("id").asLong();

        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                .content(cadastro("Outra", "outra@teste.com", "52998224725", "qualquer123")))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.erro").value("CPF já cadastrado."));
    }

    @Test @Order(3)
    void loginRejeitaSenhaErradaEAceitaCpfOuEmail() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"52998224725\",\"senha\":\"errada\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.erro").value("CPF/e-mail ou senha inválidos."));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"naoexiste@teste.com\",\"senha\":\"qualquer\"}"))
            .andExpect(status().isUnauthorized());

        tokenAna = login("529.982.247-25", "senhaDaAna1");
        tokenBeto = login("BETO@teste.com", "senhaDoBeto1");

        mvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAna))
            .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(idAna));
    }

    @Test @Order(4)
    void reclamacaoExigeTokenEGeraProtocoloEmAnalise() throws Exception {
        String req = "{\"categoriaId\":1,\"descricao\":\"Buraco grande.\",\"endereco\":\"Rua A, 10\"}";
        mvc.perform(post("/api/reclamacoes").contentType(MediaType.APPLICATION_JSON).content(req))
            .andExpect(status().isUnauthorized());

        // usuarioId enviado pelo cliente deve ser IGNORADO: o dono é sempre o usuário do token.
        String reqComUsuarioForjado = "{\"usuarioId\":%d,\"categoriaId\":1,\"descricao\":\"Buraco grande.\",\"endereco\":\"Rua A, 10\"}".formatted(idBeto);
        var r = mvc.perform(post("/api/reclamacoes").contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAna).content(reqComUsuarioForjado))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("EM_ANALISE"))
            .andExpect(jsonPath("$.codigo", matchesPattern("PC-\\d{8}-[0-9A-F]{8}")))
            .andExpect(jsonPath("$.nomeSolicitante").value("Ana Teste"))
            .andExpect(jsonPath("$.categoria").value("Infraestrutura"))
            .andExpect(jsonPath("$.tipoProblema").value("Buraco na rua"))
            .andExpect(jsonPath("$.criadoEm").exists());
        protocoloAnaId = body(r).get("id").asLong();
        protocoloAnaCodigo = body(r).get("codigo").asText();

        mvc.perform(post("/api/reclamacoes").contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAna)
                .content("{\"categoriaId\":99,\"descricao\":\"x\",\"endereco\":\"y\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.erro").value("Categoria inválida ou inativa."));
    }

    @Test @Order(5)
    void donoConsultaSeusProtocolos() throws Exception {
        var auth = "Bearer " + tokenAna;
        mvc.perform(get("/api/protocolos/" + protocoloAnaId).header(HttpHeaders.AUTHORIZATION, auth))
            .andExpect(status().isOk()).andExpect(jsonPath("$.codigo").value(protocoloAnaCodigo));
        mvc.perform(get("/api/protocolos/codigo/" + protocoloAnaCodigo.toLowerCase()).header(HttpHeaders.AUTHORIZATION, auth))
            .andExpect(status().isOk());
        mvc.perform(get("/api/protocolos/usuario/" + idAna).header(HttpHeaders.AUTHORIZATION, auth))
            .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/api/protocolos/meus").header(HttpHeaders.AUTHORIZATION, auth))
            .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test @Order(6)
    void outroCidadaoNaoAcessaProtocolosAlheios() throws Exception {
        var auth = "Bearer " + tokenBeto;
        mvc.perform(get("/api/protocolos/" + protocoloAnaId).header(HttpHeaders.AUTHORIZATION, auth))
            .andExpect(status().isNotFound());
        mvc.perform(get("/api/protocolos/codigo/" + protocoloAnaCodigo).header(HttpHeaders.AUTHORIZATION, auth))
            .andExpect(status().isNotFound());
        mvc.perform(get("/api/protocolos/usuario/" + idAna).header(HttpHeaders.AUTHORIZATION, auth))
            .andExpect(status().isForbidden());
        mvc.perform(get("/api/protocolos/meus").header(HttpHeaders.AUTHORIZATION, auth))
            .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/usuarios/" + idAna).header(HttpHeaders.AUTHORIZATION, auth))
            .andExpect(status().isForbidden());
        mvc.perform(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, auth))
            .andExpect(status().isForbidden());
    }

    @Test @Order(7)
    void tokenInvalidoEhRejeitado() throws Exception {
        mvc.perform(get("/api/protocolos/meus").header(HttpHeaders.AUTHORIZATION, "Bearer abc.def.ghi"))
            .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/protocolos/abc").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAna))
            .andExpect(status().isBadRequest());
    }

    @Test @Order(8)
    void corsPermiteFrontendLocal() throws Exception {
        mvc.perform(options("/api/protocolos/meus")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));
        mvc.perform(options("/api/protocolos/meus")
                .header(HttpHeaders.ORIGIN, "http://site-malicioso.com")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
            .andExpect(status().isForbidden());
    }
}
