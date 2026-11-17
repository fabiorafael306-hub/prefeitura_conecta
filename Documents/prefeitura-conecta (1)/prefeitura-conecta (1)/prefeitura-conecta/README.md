# Prefeitura Conecta — MVP do painel do cidadão

Plataforma inspirada nos serviços 156 das prefeituras. Este MVP cobre **somente o painel do cidadão**: cadastro, login, registro de reclamação com protocolo automático e acompanhamento em "Meus protocolos".

```
prefeitura-conecta/
├── backend/    Java 21 + Spring Boot 3.5.7 + Spring Security (JWT) + PostgreSQL
├── frontend/   React 18 + Vite 5 + TypeScript
├── database/   schema.sql (banco novo) e migracao-001-autenticacao.sql (banco existente)
└── docs/       ENDPOINTS.md (API com exemplos) e RELATORIO.md (alterações, testes, pendências)
```

## Requisitos
- JDK 21 e Maven 3.9+
- PostgreSQL 14+
- Node.js 18+ (testado com Node 22) e npm

## 1. Banco de dados

**Banco novo:**
```bash
psql -U postgres -c "CREATE DATABASE prefeitura_conecta;"
psql -U postgres -d prefeitura_conecta -f database/schema.sql
```

**Já tinha o banco da versão anterior?** Rode só a migração (adiciona a coluna `senha_hash`):
```bash
psql -U postgres -d prefeitura_conecta -f database/migracao-001-autenticacao.sql
```
> Cidadãos cadastrados antes da migração ficam sem senha e não conseguem entrar. Em desenvolvimento, apague esses registros de teste e cadastre de novo pelo frontend.

## 2. Backend
```bash
cd backend
cp .env.example .env          # preencha DB_PASSWORD e JWT_SECRET
openssl rand -base64 48       # use a saída como JWT_SECRET
mvn spring-boot:run
```
API em `http://localhost:8080`. O `.env` está no `.gitignore` — **não envie ao GitHub**.

Sem `JWT_SECRET` (mínimo 32 caracteres) a aplicação não sobe, de propósito, com uma mensagem explicando o motivo.

Testes automatizados (usam H2 em memória, não precisam do PostgreSQL):
```bash
mvn test
```

## 3. Frontend
```bash
cd frontend
npm install
npm run dev
```
Abra `http://localhost:5173`. Em desenvolvimento o Vite encaminha `/api` para `http://localhost:8080` (configurável em `.env.local`, veja `.env.example`).

Outros comandos: `npm test` (testes unitários), `npm run build` (gera `dist/`).

## 4. Fluxo para testar manualmente
1. Acesse `/cadastro`, crie um cidadão (CPF precisa ter dígitos verificadores válidos).
2. Você entra automaticamente e cai em **Nova reclamação**: escolha área e tipo (vêm de `GET /api/catalogo/categorias`), descreva e informe o endereço.
3. O protocolo gerado aparece na tela, com status **Em análise**.
4. Em **Meus protocolos**, veja a lista, filtre por status, busque pelo número e abra os detalhes.
5. Crie um segundo cidadão em outra janela anônima e confirme que ele não vê os protocolos do primeiro.
6. Confira no banco: `SELECT * FROM protocolos;` e `SELECT id, nome, senha_hash FROM usuarios;` (a senha aparece só como hash `$2a$...`).

Detalhes da API: [docs/ENDPOINTS.md](docs/ENDPOINTS.md). O que mudou e o que falta: [docs/RELATORIO.md](docs/RELATORIO.md).
