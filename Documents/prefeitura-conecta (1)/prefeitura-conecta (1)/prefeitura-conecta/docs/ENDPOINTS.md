# API — Prefeitura Conecta (painel do cidadão)

Base: `http://localhost:8080`. Todas as respostas são JSON.

**Autenticação:** endpoints marcados com 🔒 exigem o header
`Authorization: Bearer <token>`, obtido em `POST /api/auth/login`. O token expira em 60 minutos (configurável em `JWT_EXPIRACAO_MINUTOS`).

**Formato de erro (todos os endpoints):**
```json
{ "erro": "Mensagem para o usuário" }
```
Erros de validação (400) trazem também os campos:
```json
{ "erro": "Dados inválidos", "detalhes": [ { "campo": "cpf", "mensagem": "CPF inválido." } ] }
```

| Status | Quando |
|---|---|
| 400 | Campos inválidos, JSON malformado, parâmetro com tipo errado, categoria inexistente/inativa |
| 401 | Sem token, token inválido/expirado, login com credenciais erradas |
| 403 | Autenticado, mas sem permissão (ex.: listar protocolos de outro `usuarioId`) |
| 404 | Recurso não existe **ou pertence a outro cidadão** (não revela que existe) |
| 409 | CPF ou e-mail já cadastrado |
| 500 | Erro interno (detalhes só no log do servidor) |

---

## Catálogo

### `GET /api/catalogo/categorias` — público
Categorias ativas, ordenadas por área e tipo.
```json
[
  { "id": 1, "codigoArea": "INFRAESTRUTURA", "nomeArea": "Infraestrutura", "codigoTipo": "BURACO_NA_RUA", "nomeTipo": "Buraco na rua" }
]
```
O frontend agrupa por `codigoArea` e envia o `id` escolhido como `categoriaId`.

---

## Cidadão e autenticação

### `POST /api/usuarios` — público (cadastro)
```http
POST /api/usuarios
Content-Type: application/json

{"nome":"Maria Souza","email":"maria@exemplo.com","cpf":"52998224725","endereco":"Rua das Acácias, 45","senha":"minhaSenha123"}
```
Regras: `nome` obrigatório (≤150); `email` válido e único (≤180, salvo em minúsculas); `cpf` com 11 dígitos e dígitos verificadores válidos, único; `endereco` opcional (≤250); `senha` de 8 a 72 caracteres, guardada como hash BCrypt.

`201 Created`
```json
{ "id": 1, "nome": "Maria Souza", "email": "maria@exemplo.com", "cpf": "52998224725", "endereco": "Rua das Acácias, 45", "tipoUsuario": "CIDADAO" }
```
A senha e o hash nunca são devolvidos.

### `POST /api/auth/login` — público
`login` aceita CPF (com ou sem pontuação) ou e-mail.
```http
POST /api/auth/login
Content-Type: application/json

{"login":"52998224725","senha":"minhaSenha123"}
```
`200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "expiraEm": "2026-11-15T18:00:00Z",
  "usuario": { "id": 1, "nome": "Maria Souza", "email": "maria@exemplo.com", "cpf": "52998224725", "endereco": "Rua das Acácias, 45", "tipoUsuario": "CIDADAO" }
}
```
`401` → `{"erro":"CPF/e-mail ou senha inválidos."}` (mesma mensagem se o usuário não existir).

### `GET /api/auth/me` 🔒
Dados do cidadão dono do token. Usado pelo frontend ao recarregar a página.

### `GET /api/usuarios/{id}` 🔒
Só o próprio cidadão (ou ADMIN). Outro id → `403`.

### `GET /api/usuarios` 🔒 ADMIN
Lista todos os cidadãos. Restrito a `tipo_usuario = 'ADMIN'` porque expõe CPF e e-mail de todos. Cidadão → `403`.

---

## Reclamação e protocolo

### `POST /api/reclamacoes` 🔒
O cidadão é identificado **pelo token**. Não envie `usuarioId` (se enviado, é ignorado).
```http
POST /api/reclamacoes
Authorization: Bearer <token>
Content-Type: application/json

{"categoriaId":1,"descricao":"Buraco grande perto da faixa de pedestres.","endereco":"Rua das Acácias, 45"}
```
`201 Created` — reclamação e protocolo são gravados na mesma transação:
```json
{
  "id": 1,
  "codigo": "PC-20261115-A1B2C3D4",
  "status": "EM_ANALISE",
  "criadoEm": "2026-11-15T14:50:12.345678",
  "reclamacaoId": 1,
  "nomeSolicitante": "Maria Souza",
  "categoria": "Infraestrutura",
  "tipoProblema": "Buraco na rua",
  "descricao": "Buraco grande perto da faixa de pedestres.",
  "endereco": "Rua das Acácias, 45"
}
```
`criadoEm` é `LocalDateTime` sem fuso (horário do servidor).

### `GET /api/protocolos/meus` 🔒 (novo, recomendado)
Protocolos do cidadão do token, do mais recente para o mais antigo.

### `GET /api/protocolos/usuario/{usuarioId}` 🔒
Mantido por compatibilidade. Só aceita o `usuarioId` do próprio token (ou ADMIN); outro id → `403`.

### `GET /api/protocolos/{id}` 🔒
### `GET /api/protocolos/codigo/{codigo}` 🔒
Detalhe de um protocolo. A busca por código não diferencia maiúsculas/minúsculas. Protocolo de outro cidadão → `404`.

Status possíveis: `EM_ANALISE`, `EM_ABERTO`, `CONCLUIDO`. Não há endpoint para o cidadão alterar status.

---

## Exemplos com curl
```bash
curl -s localhost:8080/api/catalogo/categorias

curl -s -X POST localhost:8080/api/usuarios -H 'Content-Type: application/json' \
  -d '{"nome":"Maria Souza","email":"maria@exemplo.com","cpf":"52998224725","senha":"minhaSenha123"}'

TOKEN=$(curl -s -X POST localhost:8080/api/auth/login -H 'Content-Type: application/json' \
  -d '{"login":"52998224725","senha":"minhaSenha123"}' | sed -E 's/.*"token":"([^"]+)".*/\1/')

curl -s -X POST localhost:8080/api/reclamacoes -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"categoriaId":1,"descricao":"Buraco grande.","endereco":"Rua A, 10"}'

curl -s localhost:8080/api/protocolos/meus -H "Authorization: Bearer $TOKEN"
```
O arquivo `backend/requests-exemplos.http` tem os mesmos exemplos para o REST Client do VS Code / IntelliJ.
