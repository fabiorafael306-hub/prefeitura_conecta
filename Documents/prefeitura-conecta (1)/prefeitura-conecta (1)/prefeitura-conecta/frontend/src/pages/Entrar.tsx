import { useState, type FormEvent } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { ApiError } from '../api/client';
import { CampoTexto } from '../components/Campo';
import { Aviso } from '../components/Aviso';

export function Entrar() {
  const { entrar, sessaoExpirada } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const destino = (location.state as { de?: string } | null)?.de ?? '/meus-protocolos';
  const recemCadastrado = (location.state as { cadastrado?: boolean } | null)?.cadastrado;

  const [login, setLogin] = useState('');
  const [senha, setSenha] = useState('');
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  async function enviar(e: FormEvent) {
    e.preventDefault();
    if (!login.trim() || !senha) { setErro('Informe seu CPF ou e-mail e sua senha.'); return; }
    setErro(null); setEnviando(true);
    try {
      await entrar(login.trim(), senha);
      navigate(destino, { replace: true });
    } catch (err) {
      setErro(err instanceof ApiError ? err.message : 'Não foi possível entrar.');
    } finally {
      setEnviando(false);
    }
  }

  return (
    <section className="cartao-estreito">
      <h1>Entrar</h1>
      <p className="introducao">Acompanhe suas solicitações e registre novos problemas na sua rua ou bairro.</p>
      {sessaoExpirada && <Aviso>Sua sessão expirou. Entre novamente para continuar.</Aviso>}
      {recemCadastrado && <Aviso tipo="sucesso">Cadastro concluído. Entre com seu CPF ou e-mail e a senha que você criou.</Aviso>}
      {erro && <Aviso tipo="erro">{erro}</Aviso>}
      <form onSubmit={enviar} noValidate>
        <CampoTexto rotulo="CPF ou e-mail" value={login} onChange={(e) => setLogin(e.target.value)}
          autoComplete="username" required autoFocus />
        <CampoTexto rotulo="Senha" type="password" value={senha} onChange={(e) => setSenha(e.target.value)}
          autoComplete="current-password" required />
        <button type="submit" className="botao" disabled={enviando}>{enviando ? 'Entrando…' : 'Entrar'}</button>
      </form>
      <p className="troca">Ainda não tem cadastro? <Link to="/cadastro">Criar cadastro</Link></p>
    </section>
  );
}
