import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { authApi } from '../api/auth';
import { setUnauthorizedHandler, tokenStorage } from '../api/client';
import type { Usuario } from '../api/types';

interface AuthState {
  usuario: Usuario | null;
  carregando: boolean;
  /** true quando o logout foi causado por token expirado/inválido (para avisar na tela de login). */
  sessaoExpirada: boolean;
  entrar: (login: string, senha: string) => Promise<void>;
  sair: () => void;
}

const AuthContext = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [sessaoExpirada, setSessaoExpirada] = useState(false);

  const sair = useCallback(() => {
    tokenStorage.clear();
    setUsuario(null);
  }, []);

  // Qualquer 401 em rota autenticada derruba a sessão.
  useEffect(() => {
    setUnauthorizedHandler(() => {
      tokenStorage.clear();
      setUsuario(null);
      setSessaoExpirada(true);
    });
    return () => setUnauthorizedHandler(null);
  }, []);

  // Ao abrir/recarregar a página, revalida o token salvo com o backend (fonte da verdade).
  useEffect(() => {
    if (!tokenStorage.get()) { setCarregando(false); return; }
    authApi.me()
      .then(setUsuario)
      .catch(() => tokenStorage.clear())
      .finally(() => setCarregando(false));
  }, []);

  const entrar = useCallback(async (login: string, senha: string) => {
    const r = await authApi.login({ login, senha });
    tokenStorage.set(r.token);
    setSessaoExpirada(false);
    setUsuario(r.usuario);
  }, []);

  const value = useMemo(() => ({ usuario, carregando, sessaoExpirada, entrar, sair }), [usuario, carregando, sessaoExpirada, entrar, sair]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth deve ser usado dentro de <AuthProvider>.');
  return ctx;
}
