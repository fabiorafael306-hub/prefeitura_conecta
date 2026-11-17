# Relatório — Integração frontend/backend do MVP do cidadão

Data: 15/11/2026

## 1. Análise técnica do material recebido

**Recebido:** zip do backend (`prefeitura-conecta-mvp`) e `prefeitura-conecta-schema.sql` (idêntico a `src/main/resources/db/schema.sql`, conferido com `diff`).
**Não recebido:** nenhum arquivo de frontend. O frontend foi criado do zero.

### Backend original
- Java 21, Spring Boot 3.5.7, Maven, Spring Web, Spring Data JPA, Bean Validation, driver PostgreSQL.
- Arquitetura modular por domínio: `auth`, `catalogo`, `reclamacao`, `protocolo`, `config`, `shared` — cada um com `controller/dto/entity/repository/service`. **Preservada.**
- Conexão: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` com valores padrão; `ddl-auto=validate`.

### Modelo de dados
```
usuarios 1 ──< reclamacoes >── 1 categorias_problema
                   │
                   1
                   │
               protocolos (1:1 com reclamação, código único, status com CHECK)
```

### Endpoints originais
`GET /api/catalogo/categorias` · `POST/GET /api/usuarios` · `GET /api/usuarios/{id}` · `POST /api/reclamacoes` · `GET /api/protocolos/{id}` · `GET /api/protocolos/codigo/{codigo}` · `GET /api/protocolos/usuario/{usuarioId}`

### Problemas encontrados
| # | Problema | Gravidade |
|---|---|---|
| 1 | Sem autenticação: qualquer pessoa consultava protocolos de qualquer cidadão trocando o ID na URL | Crítica |
| 2 | `POST /api/reclamacoes` aceitava `usuarioId` do corpo: dava para registrar reclamação em nome de outro | Crítica |
| 3 | `GET /api/usuarios` público, devolvendo nome, CPF, e-mail e endereço de todos | Crítica |
| 4 | CORS em `WebMvcConfigurer`: ao adicionar Spring Security, o preflight com `Authorization` seria bloqueado | Alta (ao integrar) |
| 5 | `IllegalArgumentException` → 409 para tudo, inclusive erros que não são conflito | Média |
| 6 | Sem tratamento para JSON malformado, tipo errado na URL, violação de UNIQUE concorrente e erro genérico (Spring devolveria corpo fora do padrão ou stack trace) | Média |
| 7 | CPF validado só por formato (11 números), sem dígitos verificadores | Média |
| 8 | Categoria inválida respondia 404 num POST | Baixa |
| 9 | Senha padrão `COLOQUE_SUA_SENHA_AQUI` no `application.properties` e `show-sql=true` fixo | Baixa |
| 10 | Índices `idx_protocolos_reclamacao` e `idx_protocolos_codigo` duplicam os índices que as constraints `UNIQUE` já criam | Baixa (não alterado) |

Formatos JSON entre frontend e backend: não havia frontend para comparar; os tipos em `frontend/src/api/types.ts` foram escritos a partir dos records Java, campo a campo.

## 2. Alterações no backend

### Autenticação (novo)
- **Spring Security + OAuth2 Resource Server** com JWT HS256 assinado pelo segredo `JWT_SECRET` (mínimo 32 caracteres; a aplicação não sobe sem ele).
- **Senha com BCrypt** (`senha_hash`); nunca em texto puro, nunca devolvida pela API. `toString()` dos DTOs mascara a senha para não vazar em log.
- `POST /api/auth/login` (CPF ou e-mail + senha) e `GET /api/auth/me`.
- Login com mensagem genérica e comparação BCrypt mesmo quando o usuário não existe, para não revelar quais CPFs/e-mails estão cadastrados.
- Token contém só `sub` (id) e `papel`; não contém CPF/e-mail (JWT é legível por qualquer um).
- Por que JWT e não sessão: a API já era stateless e consumida por SPA em outra porta; JWT encaixa sem mudar a arquitetura e sem lidar com cookies cross-origin/CSRF.

### Isolamento entre cidadãos
- `POST /api/reclamacoes`: `usuarioId` **removido do DTO**; o dono vem do token. Justificativa: manter o campo seria manter a falha #2. Clientes antigos que ainda enviam o campo não quebram (o Jackson ignora campos desconhecidos).
- `GET /api/protocolos/{id}` e `/codigo/{codigo}`: verificação de posse no `ProtocoloService`; protocolo alheio → **404** (não confirma que existe).
- `GET /api/protocolos/usuario/{usuarioId}`: **mantido** (contrato original), mas só aceita o próprio id → senão **403**.
- `GET /api/protocolos/meus`: **novo**, atalho que dispensa passar id.
- `GET /api/usuarios/{id}`: só o próprio cidadão.
- `GET /api/usuarios`: **mantido**, mas restrito ao papel ADMIN (justificativa: falha #3). Não foi criada nenhuma tela/fluxo administrativo.

### Validação e erros
- `@CPF` (Hibernate Validator) além do padrão de 11 dígitos; mensagens em português em todos os campos.
- E-mail normalizado para minúsculas no cadastro; busca no login sem diferenciar maiúsculas.
- Nova `ConflitoException` (409) para CPF/e-mail duplicados; `IllegalArgumentException` passou a 400.
- Novos tratamentos: 401 (credenciais), 403, JSON inválido (400), tipo de parâmetro (400), `DataIntegrityViolation` (409), rota inexistente (404), método não suportado (405), genérico (500 sem detalhes, com log).
- 401/403 dos filtros de segurança também saem no formato `{"erro": ...}`.
- Categoria inválida/inativa: 404 → 400.
- Código do protocolo normalizado para maiúsculas na busca; descrição e endereço com `trim()`.

### Configuração
- CORS movido para `CorsConfigurationSource` integrado ao Spring Security; origens configuráveis (`CORS_ALLOWED_ORIGINS`), mantendo os padrões originais.
- `application.properties` sem senha padrão; lê `.env` local opcional (`spring.config.import`), com `.env.example` e `.gitignore`.
- `show-sql` controlável por `SHOW_SQL` (padrão false).

### Banco de dados
- Coluna `usuarios.senha_hash VARCHAR(100)` (nula só para cadastros antigos).
- Índice `idx_reclamacoes_categoria`.
- `database/schema.sql` (completo, para banco novo) e `database/migracao-001-autenticacao.sql` (para banco existente). Ambos idempotentes.

### Arquivos do backend
**Novos:** `config/SecurityConfig.java`, `config/JwtConfig.java`, `auth/controller/AuthController.java`, `auth/service/AuthService.java`, `auth/service/TokenService.java`, `auth/dto/LoginRequest.java`, `auth/dto/LoginResponse.java`, `shared/security/UsuarioAutenticado.java`, `shared/exception/ConflitoException.java`, `shared/exception/CredenciaisInvalidasException.java`, `db/migracao-001-autenticacao.sql`, `.env.example`, `.gitignore`, `src/test/**`.
**Alterados:** `pom.xml`, `application.properties`, `schema.sql`, `CorsConfig`, `Usuario`, `UsuarioRepository`, `UsuarioRequest`, `UsuarioService`, `UsuarioController`, `ReclamacaoRequest`, `ReclamacaoController`, `ReclamacaoService`, `ProtocoloService`, `ProtocoloController`, `ApiExceptionHandler`, `README.md`, `requests-exemplos.http`.
**Inalterados:** entidades `CategoriaProblema`, `Reclamacao`, `Protocolo`, `StatusProtocolo`; repositórios de catálogo, reclamação e protocolo; `CatalogoController`; `ProtocoloResponse`; `UsuarioResponse`.
**Removido:** nada.

## 3. Frontend (novo)
React 18 + Vite 5 + TypeScript, com camada de API isolada em `src/api/`. Telas: Entrar, Criar cadastro, Nova reclamação (área → tipo carregados do catálogo, envio de `categoriaId`), recibo do protocolo, Meus protocolos (lista, filtro por status, busca por número, atualizar) e detalhe com andamento dos três status (somente leitura). Detalhes em `frontend/README.md`.

Identidade visual baseada na sinalização urbana (placa de rua azul, fonte condensada), com Atkinson Hyperlegible no texto para facilitar a leitura. Responsivo, com foco visível de teclado, rótulos acessíveis e respeito a `prefers-reduced-motion`.

## 4. Testes — o que foi executado de verdade

> Limitação do ambiente: o acesso ao Maven Central (`repo.maven.apache.org`) estava bloqueado. Por isso **o backend não foi compilado nem executado** aqui. Tudo que depende do Spring rodando está na seção "Não executado".

### Executado ✅
| Teste | Como | Resultado |
|---|---|---|
| `schema.sql` em banco novo, rodado 2× (idempotência) | PostgreSQL 16 real | OK |
| Schema **original** + cidadão antigo → `migracao-001` rodada 2× | PostgreSQL 16 real | OK; cidadão antigo preservado com `senha_hash` nula |
| Estrutura final do banco novo = banco migrado | Comparação de `information_schema.columns` | Idênticas |
| 10 categorias carregadas pelo seed | Consulta SQL | OK |
| Sintaxe de todos os arquivos Java | `javac` sem dependências, filtrando só erros de parsing | Nenhum erro de sintaxe (erros de símbolo são esperados sem as libs) |
| Typecheck do frontend | `tsc -b` (modo strict) | OK |
| Build de produção do frontend | `vite build` | OK |
| Testes unitários do frontend (7) | `vitest run` | 7/7 passando: conversão de erro por campo, header Authorization, logout em 401 só em rota autenticada, erro de rede, agrupamento do catálogo, máscara de CPF, leitura de data com microssegundos |
| Revisão visual das telas (desktop e celular) | Chrome headless contra um **stub temporário** que imita o contrato da API | Telas renderizam e o fluxo navega. ⚠️ Isso **não** testa o backend real; o stub não foi incluído na entrega |

### Escrito, mas NÃO executado ⚠️
`backend/src/test/java/.../FluxoCidadaoIntegrationTest.java` (rode com `mvn test`). Cobre:

| # pedido | Teste | Status |
|---|---|---|
| 1 | Cadastro (validação de campos, CPF inválido, senha guardada como hash, CPF duplicado → 409) | Não executado |
| 2 | Catálogo público, só categorias ativas | Não executado |
| 4–5 | Reclamação exige token; gera protocolo `EM_ANALISE` com código `PC-AAAAMMDD-XXXXXXXX`; `usuarioId` forjado é ignorado; categoria inativa → 400 | Não executado |
| 6–7 | Consulta por ID, por código (minúsculas), `/usuario/{id}` e `/meus` | Não executado |
| 9 | Login errado → 401; token inválido → 401; id não numérico → 400 | Não executado |
| 10 | Outro cidadão: protocolo alheio → 404, `/usuario/{outro}` → 403, `/meus` vazio, `/usuarios/{outro}` → 403, `/usuarios` → 403 | Não executado |
| — | CORS: `localhost:5173` liberado, origem estranha → 403 | Não executado |

### Não executado ❌
- Item 3 (seleção de categoria) e item 8 (persistência no PostgreSQL) **ponta a ponta** — frontend + backend real + PostgreSQL juntos.
- Subida da aplicação com `mvn spring-boot:run` e validação do `ddl-auto=validate` contra o schema novo.

**Recomendação:** rode `mvn test` e depois o roteiro do `README.md` (seção 4) antes de considerar o MVP validado. Se algum teste falhar, me mande a saída.

## 5. Pendências e limitações conhecidas
1. **Backend não compilado neste ambiente** (ver seção 4).
2. **Cidadãos antigos sem senha** não conseguem entrar; não existe fluxo de "definir/recuperar senha".
3. **Sem limite de tentativas de login** (rate limiting) — necessário antes de publicar.
4. **Token em `sessionStorage`**: vulnerável se houver XSS. Alternativa mais segura para produção: cookie `HttpOnly` + `SameSite` servido no mesmo domínio.
5. **Sem refresh token**: após 60 min o cidadão precisa entrar de novo.
6. **Sem logout no servidor**: o JWT continua válido até expirar mesmo após "Sair".
7. **ADMIN só via SQL**; não há painel administrativo (fora do escopo). Sem ADMIN, nenhum status muda de `EM_ANALISE`.
8. **Datas sem fuso** (`LocalDateTime`, horário do servidor). Em produção, configure o fuso do servidor (`America/Sao_Paulo`) ou migre para `OffsetDateTime`.
9. **Código do protocolo** usa 8 caracteres de um UUID; colisão é improvável e a constraint `UNIQUE` impede duplicata, mas não há nova tentativa automática — o cidadão receberia erro 409.
10. **LGPD**: CPF é dado pessoal; falta política de privacidade, consentimento e mascaramento em telas/logs futuros.
11. **Sem migrações versionadas** (Flyway/Liquibase); scripts SQL aplicados manualmente.
12. Índices redundantes em `protocolos` (item 10 da análise) não foram removidos para não alterar o schema além do necessário.
13. Não há histórico de movimentações do protocolo, upload de fotos nem notificações (fora do escopo).
