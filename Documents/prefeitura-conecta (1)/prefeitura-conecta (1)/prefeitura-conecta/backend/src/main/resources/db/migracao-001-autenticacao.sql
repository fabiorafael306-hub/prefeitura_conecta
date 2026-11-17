-- Migração 001 — autenticação do cidadão
-- Aplicar em bancos criados com o schema original (antes da autenticação). Idempotente: pode rodar mais de uma vez.
BEGIN;

ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS senha_hash VARCHAR(100);

-- Índice para a FK de categoria (consultas/joins por categoria).
CREATE INDEX IF NOT EXISTS idx_reclamacoes_categoria ON reclamacoes(categoria_id);

COMMIT;

-- ATENÇÃO: cidadãos cadastrados antes desta migração ficam com senha_hash NULL e NÃO conseguem fazer login.
-- Não há fluxo de "definir/recuperar senha" no MVP. Em ambiente de desenvolvimento, a opção mais simples é
-- recadastrá-los (removendo antes os registros de teste) — veja docs/RELATORIO.md, seção Pendências.
