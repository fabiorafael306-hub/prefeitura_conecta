# Prefeitura Conecta — Backend (painel do cidadão)

Backend REST em Java 21, Spring Boot 3.5.7, Maven, Spring Security (JWT HS256) e PostgreSQL.

## Escopo
- Cadastro de cidadão com senha (hash BCrypt) e login por CPF ou e-mail.
- Autenticação stateless com JWT; o cidadão é sempre identificado pelo token.
- Catálogo de categorias e tipos de problema predefinidos (público).
- Registro de reclamação com geração automática do protocolo na mesma transação; status inicial `EM_ANALISE`.
- Consulta de protocolo por ID e por código, e listagem "Meus protocolos" — com isolamento entre cidadãos.
- CORS configurável (padrão `localhost:5173` e `localhost:3000`) e erros padronizados.

Fora do escopo: painel administrativo, gestão de órgãos, notificações, alteração de status pelo cidadão, upload de fotos.

## Módulos (pacote `com.prefeitura.conecta`)
| Pacote | Responsabilidade |
|---|---|
| `auth` | Cidadão, cadastro, login, emissão de token |
| `catalogo` | Categorias/tipos de problema |
| `reclamacao` | Registro da reclamação |
| `protocolo` | Protocolo e consultas com verificação de posse |
| `config` | Segurança, JWT e CORS |
| `shared` | Tratamento de erros, exceções e `UsuarioAutenticado` |

## Executar
1. Crie o banco e rode `src/main/resources/db/schema.sql` (ou, se o banco já existe, `db/migracao-001-autenticacao.sql`).
2. `cp .env.example .env` e preencha `DB_PASSWORD` e `JWT_SECRET` (gere com `openssl rand -base64 48`).
3. `mvn spring-boot:run` → `http://localhost:8080`.

Variáveis (no `.env` ou no ambiente): `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRACAO_MINUTOS` (60), `CORS_ALLOWED_ORIGINS`, `SHOW_SQL` (false), `SERVER_PORT` (8080).

O Hibernate usa `ddl-auto=validate`: ele confere as tabelas, mas não as cria.

## Testes
```bash
mvn test
```
`FluxoCidadaoIntegrationTest` sobe a aplicação com H2 em memória (modo PostgreSQL) e cobre cadastro, login, reclamação, protocolo, isolamento entre cidadãos, token inválido e CORS.

## API
Veja `../docs/ENDPOINTS.md` e `requests-exemplos.http`.

## Contas ADMIN
Não há tela nem endpoint para criar ADMIN (fora do escopo). O papel existe só para proteger `GET /api/usuarios` e preparar o futuro painel administrativo. Para testes, promova manualmente: `UPDATE usuarios SET tipo_usuario='ADMIN' WHERE id=...;` e faça login de novo.
