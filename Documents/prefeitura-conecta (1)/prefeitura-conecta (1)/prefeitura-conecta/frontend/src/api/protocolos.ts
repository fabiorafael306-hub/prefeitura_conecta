import { request } from './client';
import type { Protocolo, ReclamacaoRequest } from './types';

export const protocolosApi = {
  /** POST /api/reclamacoes — registra e devolve o protocolo gerado (201) */
  registrarReclamacao: (dados: ReclamacaoRequest) =>
    request<Protocolo>('/api/reclamacoes', { method: 'POST', body: dados }),
  /** GET /api/protocolos/usuario/{usuarioId} — o backend só aceita o id do próprio token */
  listarDoUsuario: (usuarioId: number, signal?: AbortSignal) =>
    request<Protocolo[]>(`/api/protocolos/usuario/${usuarioId}`, { signal }),
  /** GET /api/protocolos/{id} */
  buscarPorId: (id: number, signal?: AbortSignal) => request<Protocolo>(`/api/protocolos/${id}`, { signal }),
  /** GET /api/protocolos/codigo/{codigo} */
  buscarPorCodigo: (codigo: string) =>
    request<Protocolo>(`/api/protocolos/codigo/${encodeURIComponent(codigo.trim())}`),
};
