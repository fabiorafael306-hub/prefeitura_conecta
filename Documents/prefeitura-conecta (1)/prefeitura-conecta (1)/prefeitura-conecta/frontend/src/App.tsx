import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider, useAuth } from './auth/AuthContext';
import { RotaProtegida, RotaPublica } from './auth/rotas';
import { Layout } from './components/Layout';
import { Carregando } from './components/Carregando';
import { Entrar } from './pages/Entrar';
import { Cadastro } from './pages/Cadastro';
import { NovaReclamacao } from './pages/NovaReclamacao';
import { MeusProtocolos } from './pages/MeusProtocolos';
import { DetalheProtocolo } from './pages/DetalheProtocolo';
import { NaoEncontrada } from './pages/NaoEncontrada';

function Inicio() {
  const { usuario, carregando } = useAuth();
  if (carregando) return <Carregando />;
  return <Navigate to={usuario ? '/meus-protocolos' : '/entrar'} replace />;
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route element={<Layout />}>
            <Route index element={<Inicio />} />
            <Route element={<RotaPublica />}>
              <Route path="entrar" element={<Entrar />} />
              <Route path="cadastro" element={<Cadastro />} />
            </Route>
            <Route element={<RotaProtegida />}>
              <Route path="nova-reclamacao" element={<NovaReclamacao />} />
              <Route path="meus-protocolos" element={<MeusProtocolos />} />
              <Route path="meus-protocolos/:id" element={<DetalheProtocolo />} />
            </Route>
            <Route path="*" element={<NaoEncontrada />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
