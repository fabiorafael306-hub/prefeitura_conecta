import { request } from './client';
import type { CadastroRequest, LoginRequest, LoginResponse, Usuario } from './types';

export const authApi = {
  /** POST /api/usuarios — público */
  cadastrar: (dados: CadastroRequest) => request<Usuario>('/api/usuarios', { method: 'POST', body: dados, publica: true }),
  /** POST /api/auth/login — público */
  login: (dados: LoginRequest) => request<LoginResponse>('/api/auth/login', { method: 'POST', body: dados, publica: true }),
  /** GET /api/auth/me — autenticado */
  me: () => request<Usuario>('/api/auth/me'),
};
