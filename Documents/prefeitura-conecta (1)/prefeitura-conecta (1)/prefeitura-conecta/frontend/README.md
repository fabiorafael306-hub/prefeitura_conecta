# Prefeitura Conecta — Frontend (painel do cidadão)

React 18 + Vite 5 + TypeScript + React Router 6. Sem biblioteca de UI; CSS próprio em `src/styles.css`.

```bash
npm install
npm run dev      # http://localhost:5173 (proxy de /api para http://localhost:8080)
npm test         # testes unitários (Vitest)
npm run build    # typecheck + build de produção em dist/
```

## Estrutura
```
src/
├── api/          Única camada que fala com o backend
│   ├── client.ts     fetch, token, normalização de erros (ApiError), tratamento de 401
│   ├── types.ts      Tipos espelhando os DTOs Java
│   ├── auth.ts       cadastro, login, me
│   ├── catalogo.ts   categorias + agrupamento por área (apresentação)
│   └── protocolos.ts reclamação e consultas de protocolo
├── auth/         AuthContext (sessão) e rotas protegidas/públicas
├── components/   Layout, campos de formulário, recibo do protocolo, status
├── pages/        Entrar, Cadastro, NovaReclamacao, MeusProtocolos, DetalheProtocolo
└── utils/        Formatação (CPF, datas, rótulos de status)
```

## Decisões
- Componentes nunca chamam `fetch` direto: usam `src/api/*`.
- Validação no frontend é só de preenchimento (UX). Regras de negócio (CPF válido, unicidade, tamanhos) ficam no backend, e os erros dele aparecem campo a campo.
- Token JWT em `sessionStorage` (some ao fechar a aba). Qualquer `401` em rota autenticada encerra a sessão e leva ao login com aviso.
- Ao recarregar a página, a sessão é revalidada com `GET /api/auth/me`.
- `VITE_API_URL` permite apontar para um backend em outro domínio (produção). Vazio = usa o proxy do Vite.
