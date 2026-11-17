import { NavLink, Outlet, Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export function Layout() {
  const { usuario, sair } = useAuth();
  const primeiroNome = usuario?.nome.split(' ')[0];
  return (
    <>
      <a className="pular" href="#conteudo">Ir para o conteúdo</a>
      <header className="placa">
        <div className="placa-interna">
          <Link to="/" className="placa-marca" aria-label="Prefeitura Conecta, página inicial">
            <span className="placa-numero">CAC</span>
            <span className="placa-nome">Prefeitura Conecta</span>
          </Link>
          {usuario && (
            <div className="placa-usuario">
              <span>Olá, {primeiroNome}</span>
              <button type="button" className="botao-placa" onClick={sair}>Sair</button>
            </div>
          )}
        </div>
      </header>
      {usuario && (
        <nav className="abas" aria-label="Seções do painel">
          <div className="abas-interna">
            <NavLink to="/nova-reclamacao" className="aba">Nova reclamação</NavLink>
            <NavLink to="/meus-protocolos" className="aba">Meus protocolos</NavLink>
          </div>
        </nav>
      )}
      <main id="conteudo" className="conteudo">
        <Outlet />
      </main>
      <footer className="rodape">
        <p>Prefeitura Conecta. Painel do cidadão, versão MVP.</p>
      </footer>
    </>
  );
}
