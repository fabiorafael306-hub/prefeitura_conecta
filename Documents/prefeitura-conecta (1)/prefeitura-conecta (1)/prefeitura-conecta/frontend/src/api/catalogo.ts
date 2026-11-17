import { request } from './client';
import type { Categoria } from './types';

export interface AreaComTipos {
  codigoArea: string;
  nomeArea: string;
  tipos: Categoria[];
}

export const catalogoApi = {
  /** GET /api/catalogo/categorias — público, só categorias ativas */
  listar: (signal?: AbortSignal) => request<Categoria[]>('/api/catalogo/categorias', { publica: true, signal }),
};

/** Agrupa a lista plana do backend por área, preservando a ordem recebida. Apenas apresentação. */
export function agruparPorArea(categorias: Categoria[]): AreaComTipos[] {
  const mapa = new Map<string, AreaComTipos>();
  for (const c of categorias) {
    let area = mapa.get(c.codigoArea);
    if (!area) {
      area = { codigoArea: c.codigoArea, nomeArea: c.nomeArea, tipos: [] };
      mapa.set(c.codigoArea, area);
    }
    area.tipos.push(c);
  }
  return [...mapa.values()];
}
