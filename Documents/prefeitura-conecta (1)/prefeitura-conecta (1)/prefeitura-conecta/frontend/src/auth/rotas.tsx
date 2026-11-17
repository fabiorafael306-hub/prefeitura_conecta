import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from './AuthContext';
import { Carregando } from '../components/Carregando';

/** Só renderiza as rotas filhas com cidadão autenticado. A proteção real está no backend. */
export function RotaProtegida() {
  const { usuario, carregando } = useAuth();
  const location = useLocation();
  if (carregando) return <Carregando texto="Verificando sua sessão…" />;
  if (!usuario) return <Navigate to="/entrar" replace state={{ de: location.pathname }} />;
  return <Outlet />;
}

/** Login/cadastro: se já estiver logado, vai direto para os protocolos. */
export function RotaPublica() {
  const { usuario, carregando } = useAuth();
  if (carregando) return <Carregando texto="Carregando…" />;
  if (usuario) return <Navigate to="/meus-protocolos" replace />;
  return <Outlet />;
}
