import type { StatusProtocolo } from '../api/types';

export const somenteDigitos = (v: string) => v.replace(/\D/g, '');

/** 52998224725 -> 529.982.247-25 (máscara visual; o backend recebe só dígitos). */
export function formatarCpf(v: string): string {
  const d = somenteDigitos(v).slice(0, 11);
  return d
    .replace(/^(\d{3})(\d)/, '$1.$2')
    .replace(/^(\d{3})\.(\d{3})(\d)/, '$1.$2.$3')
    .replace(/\.(\d{3})(\d{1,2})$/, '.$1-$2');
}

/**
 * O backend envia LocalDateTime sem fuso (ex.: 2026-11-15T12:00:00.123456), no horário do servidor.
 * Interpretamos como horário local e limitamos a fração a milissegundos para o Date aceitar.
 */
export function parseDataBackend(v: string): Date {
  const [base, frac] = v.split('.');
  return new Date(frac ? `${base}.${frac.slice(0, 3)}` : base);
}

export function formatarData(v: string, comHora = true): string {
  const d = parseDataBackend(v);
  if (Number.isNaN(d.getTime())) return v;
  return d.toLocaleString('pt-BR', comHora
    ? { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' }
    : { day: '2-digit', month: '2-digit', year: 'numeric' });
}

export const STATUS_INFO: Record<StatusProtocolo, { rotulo: string; explicacao: string }> = {
  EM_ANALISE: { rotulo: 'Em análise', explicacao: 'A prefeitura recebeu sua solicitação e está verificando as informações.' },
  EM_ABERTO: { rotulo: 'Em aberto', explicacao: 'A solicitação foi aceita e aguarda atendimento pela equipe responsável.' },
  CONCLUIDO: { rotulo: 'Concluído', explicacao: 'O atendimento foi finalizado.' },
};
export const ORDEM_STATUS: StatusProtocolo[] = ['EM_ANALISE', 'EM_ABERTO', 'CONCLUIDO'];

export const rotuloStatus = (s: string) => STATUS_INFO[s as StatusProtocolo]?.rotulo ?? s;
