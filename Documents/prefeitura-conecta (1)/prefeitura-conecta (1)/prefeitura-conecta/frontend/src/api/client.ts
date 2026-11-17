import type { ErroApiBody } from './types';

/**
 * Cliente HTTP único da aplicação. Componentes NÃO chamam fetch diretamente:
 * usam as funções dos módulos em src/api/*, que passam por aqui.
 */

const BASE_URL = (import.meta.env.VITE_API_URL ?? '').replace(/\/$/, '');
const TOKEN_KEY = 'pc.token';

/** Erro normalizado: status HTTP + mensagem pronta para o usuário + erros por campo. */
export class ApiError extends Error {
  constructor(
    public readonly status: number,
    message: string,
    public readonly campos: Record<string, string> = {},
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

// --- Token ---------------------------------------------------------------
// sessionStorage: o token some ao fechar a aba (menor janela de exposição que localStorage).
export const tokenStorage = {
  get: (): string | null => {
    try { return sessionStorage.getItem(TOKEN_KEY); } catch { return null; }
  },
  set: (t: string) => { try { sessionStorage.setItem(TOKEN_KEY, t); } catch { /* sem storage */ } },
  clear: () => { try { sessionStorage.removeItem(TOKEN_KEY); } catch { /* sem storage */ } },
};

// Chamado quando o backend responde 401 numa rota autenticada (token expirado/inválido).
let onUnauthorized: (() => void) | null = null;
export function setUnauthorizedHandler(fn: (() => void) | null) { onUnauthorized = fn; }

const MENSAGENS_PADRAO: Record<number, string> = {
  400: 'Confira os dados informados.',
  401: 'Sua sessão expirou. Entre novamente.',
  403: 'Você não tem permissão para acessar este conteúdo.',
  404: 'Não encontramos o que você procurou.',
  409: 'Já existe um registro com esses dados.',
  500: 'O sistema está com problemas. Tente novamente em alguns minutos.',
};

/** Converte uma resposta de erro do backend em ApiError. Exportada para testes. */
export async function toApiError(res: Response): Promise<ApiError> {
  let body: Partial<ErroApiBody> = {};
  try { body = await res.json(); } catch { /* corpo vazio ou não-JSON (ex.: proxy) */ }
  const campos: Record<string, string> = {};
  for (const d of body.detalhes ?? []) {
    // Mantém a primeira mensagem de cada campo (o backend pode mandar mais de uma).
    if (!campos[d.campo]) campos[d.campo] = d.mensagem;
  }
  const msg = body.erro || MENSAGENS_PADRAO[res.status] || MENSAGENS_PADRAO[res.status >= 500 ? 500 : 400];
  return new ApiError(res.status, msg, campos);
}

interface RequestOptions {
  method?: 'GET' | 'POST';
  body?: unknown;
  /** Rota pública (cadastro, login, catálogo): não dispara logout em 401. */
  publica?: boolean;
  signal?: AbortSignal;
}

export async function request<T>(path: string, opts: RequestOptions = {}): Promise<T> {
  const headers: Record<string, string> = { Accept: 'application/json' };
  if (opts.body !== undefined) headers['Content-Type'] = 'application/json';
  const token = tokenStorage.get();
  if (token) headers.Authorization = `Bearer ${token}`;

  let res: Response;
  try {
    res = await fetch(`${BASE_URL}${path}`, {
      method: opts.method ?? 'GET',
      headers,
      body: opts.body !== undefined ? JSON.stringify(opts.body) : undefined,
      signal: opts.signal,
    });
  } catch (e) {
    if ((e as Error).name === 'AbortError') throw e;
    throw new ApiError(0, 'Não foi possível conectar ao servidor. Verifique sua internet ou se o sistema está no ar.');
  }

  if (!res.ok) {
    const err = await toApiError(res);
    if (res.status === 401 && !opts.publica) onUnauthorized?.();
    throw err;
  }
  if (res.status === 204) return undefined as T;
  return res.json() as Promise<T>;
}
