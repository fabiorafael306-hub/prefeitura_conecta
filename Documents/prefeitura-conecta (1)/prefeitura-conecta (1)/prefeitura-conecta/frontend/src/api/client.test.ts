import { afterEach, describe, expect, it, vi } from 'vitest';
import { ApiError, request, setUnauthorizedHandler, toApiError, tokenStorage } from './client';
import { agruparPorArea } from './catalogo';
import { formatarCpf, parseDataBackend } from '../utils/format';

const resposta = (status: number, body?: unknown) =>
  new Response(body === undefined ? null : JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } });

afterEach(() => { vi.restoreAllMocks(); tokenStorage.clear(); setUnauthorizedHandler(null); });

describe('toApiError', () => {
  it('converte o erro de validação do backend em erros por campo', async () => {
    const e = await toApiError(resposta(400, { erro: 'Dados inválidos', detalhes: [
      { campo: 'cpf', mensagem: 'CPF inválido.' }, { campo: 'cpf', mensagem: 'CPF deve conter 11 números' },
      { campo: 'email', mensagem: 'E-mail inválido.' }] }));
    expect(e.status).toBe(400);
    expect(e.campos).toEqual({ cpf: 'CPF inválido.', email: 'E-mail inválido.' });
  });
  it('usa mensagem padrão quando o corpo não é JSON', async () => {
    const e = await toApiError(new Response('<html>', { status: 502 }));
    expect(e.message).toMatch(/problemas/);
  });
});

describe('request', () => {
  it('envia o token no header Authorization e JSON no corpo', async () => {
    tokenStorage.set('abc');
    const f = vi.spyOn(globalThis, 'fetch').mockResolvedValue(resposta(201, { id: 1 }));
    await request('/api/reclamacoes', { method: 'POST', body: { categoriaId: 1 } });
    const [, init] = f.mock.calls[0];
    expect((init!.headers as Record<string, string>).Authorization).toBe('Bearer abc');
    expect(init!.body).toBe('{"categoriaId":1}');
  });
  it('chama o handler de sessão expirada em 401 de rota autenticada, mas não em rota pública', async () => {
    const handler = vi.fn();
    setUnauthorizedHandler(handler);
    vi.spyOn(globalThis, 'fetch').mockImplementation(async () => resposta(401, { erro: 'Faça login para continuar.' }));
    await expect(request('/api/protocolos/meus')).rejects.toBeInstanceOf(ApiError);
    expect(handler).toHaveBeenCalledTimes(1);
    await expect(request('/api/auth/login', { method: 'POST', body: {}, publica: true })).rejects.toThrow('Faça login para continuar.');
    expect(handler).toHaveBeenCalledTimes(1);
  });
  it('transforma falha de rede em ApiError com status 0', async () => {
    vi.spyOn(globalThis, 'fetch').mockRejectedValue(new TypeError('Failed to fetch'));
    await expect(request('/api/catalogo/categorias')).rejects.toMatchObject({ status: 0 });
  });
});

describe('apresentação', () => {
  it('agrupa categorias por área mantendo a ordem do backend', () => {
    const g = agruparPorArea([
      { id: 5, codigoArea: 'ILUMINACAO', nomeArea: 'Iluminação pública', codigoTipo: 'A', nomeTipo: 'Poste apagado' },
      { id: 1, codigoArea: 'INFRAESTRUTURA', nomeArea: 'Infraestrutura', codigoTipo: 'B', nomeTipo: 'Buraco na rua' },
      { id: 6, codigoArea: 'ILUMINACAO', nomeArea: 'Iluminação pública', codigoTipo: 'C', nomeTipo: 'Lâmpada quebrada' },
    ]);
    expect(g.map((a) => a.codigoArea)).toEqual(['ILUMINACAO', 'INFRAESTRUTURA']);
    expect(g[0].tipos.map((t) => t.id)).toEqual([5, 6]);
  });
  it('formata CPF e lê LocalDateTime com microssegundos', () => {
    expect(formatarCpf('52998224725')).toBe('529.982.247-25');
    expect(parseDataBackend('2026-11-15T12:30:00.123456').getMinutes()).toBe(30);
  });
});
