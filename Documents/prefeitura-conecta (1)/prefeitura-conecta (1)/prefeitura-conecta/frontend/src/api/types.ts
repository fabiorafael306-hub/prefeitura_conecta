/** Tipos espelhando exatamente os DTOs do backend (records Java). */

export type StatusProtocolo = 'EM_ANALISE' | 'EM_ABERTO' | 'CONCLUIDO';

/** CategoriaResponse */
export interface Categoria {
  id: number;
  codigoArea: string;
  nomeArea: string;
  codigoTipo: string;
  nomeTipo: string;
}

/** UsuarioResponse */
export interface Usuario {
  id: number;
  nome: string;
  email: string;
  cpf: string;
  endereco: string | null;
  tipoUsuario: string;
}

/** UsuarioRequest */
export interface CadastroRequest {
  nome: string;
  email: string;
  cpf: string;
  endereco?: string;
  senha: string;
}

/** LoginRequest — login aceita CPF ou e-mail */
export interface LoginRequest {
  login: string;
  senha: string;
}

/** LoginResponse */
export interface LoginResponse {
  token: string;
  tipo: 'Bearer';
  expiraEm: string; // ISO-8601 (Instant)
  usuario: Usuario;
}

/** ReclamacaoRequest — o cidadão vem do token, nunca do corpo. */
export interface ReclamacaoRequest {
  categoriaId: number;
  descricao: string;
  endereco: string;
}

/** ProtocoloResponse */
export interface Protocolo {
  id: number;
  codigo: string;
  status: StatusProtocolo;
  criadoEm: string; // LocalDateTime sem fuso, ex.: 2026-11-15T12:00:00.123
  reclamacaoId: number;
  nomeSolicitante: string;
  categoria: string;
  tipoProblema: string;
  descricao: string;
  endereco: string;
}

/** Corpo de erro padronizado do ApiExceptionHandler. */
export interface ErroApiBody {
  erro: string;
  detalhes?: { campo: string; mensagem: string }[];
}
