import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authApi } from '../api/auth';
import { ApiError } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import { CampoTexto } from '../components/Campo';
import { Aviso } from '../components/Aviso';
import { formatarCpf, somenteDigitos } from '../utils/format';

type Campos = 'nome' | 'email' | 'cpf' | 'endereco' | 'senha' | 'confirmacao';

/**
 * Validação aqui é apenas de preenchimento (UX). As regras de verdade — CPF válido, e-mail único,
 * tamanho de senha — são do backend, e os erros dele são mostrados campo a campo.
 */
function validarPreenchimento(f: Record<Campos, string>): Partial<Record<Campos, string>> {
  const e: Partial<Record<Campos, string>> = {};
  if (!f.nome.trim()) e.nome = 'Informe o nome.';
  if (!f.email.trim()) e.email = 'Informe o e-mail.';
  if (somenteDigitos(f.cpf).length !== 11) e.cpf = 'O CPF tem 11 números.';
  if (!f.senha) e.senha = 'Crie uma senha.';
  if (f.senha && f.confirmacao !== f.senha) e.confirmacao = 'As senhas não são iguais.';
  return e;
}

export function Cadastro() {
  const { entrar } = useAuth();
  const navigate = useNavigate();
  const [f, setF] = useState<Record<Campos, string>>({ nome: '', email: '', cpf: '', endereco: '', senha: '', confirmacao: '' });
  const [erros, setErros] = useState<Partial<Record<Campos, string>>>({});
  const [erroGeral, setErroGeral] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  const mudar = (campo: Campos) => (e: { target: { value: string } }) => {
    const valor = campo === 'cpf' ? formatarCpf(e.target.value) : e.target.value;
    setF((atual) => ({ ...atual, [campo]: valor }));
    if (erros[campo]) setErros((atual) => ({ ...atual, [campo]: undefined }));
  };

  async function enviar(ev: FormEvent) {
    ev.preventDefault();
    setErroGeral(null);
    const locais = validarPreenchimento(f);
    setErros(locais);
    if (Object.keys(locais).length) return;

    setEnviando(true);
    const cpf = somenteDigitos(f.cpf);
    try {
      await authApi.cadastrar({
        nome: f.nome.trim(), email: f.email.trim(), cpf,
        endereco: f.endereco.trim() || undefined, senha: f.senha,
      });
    } catch (err) {
      setEnviando(false);
      if (err instanceof ApiError) {
        setErros(err.campos as Partial<Record<Campos, string>>);
        setErroGeral(err.status === 400 ? 'Corrija os campos destacados.' : err.message);
      } else setErroGeral('Não foi possível concluir o cadastro.');
      return;
    }
    // Cadastro criado: entra automaticamente. Se o login falhar, manda para a tela de entrar.
    try {
      await entrar(cpf, f.senha);
      navigate('/nova-reclamacao', { replace: true });
    } catch {
      navigate('/entrar', { replace: true, state: { cadastrado: true } });
    }
  }

  return (
    <section className="cartao-estreito">
      <h1>Criar cadastro</h1>
      <p className="introducao">Com o cadastro você registra reclamações e acompanha o andamento de cada protocolo.</p>
      {erroGeral && <Aviso tipo="erro">{erroGeral}</Aviso>}
      <form onSubmit={enviar} noValidate>
        <CampoTexto rotulo="Nome completo" value={f.nome} onChange={mudar('nome')} erro={erros.nome} autoComplete="name" maxLength={150} required />
        <CampoTexto rotulo="E-mail" type="email" value={f.email} onChange={mudar('email')} erro={erros.email} autoComplete="email" maxLength={180} required />
        <CampoTexto rotulo="CPF" value={f.cpf} onChange={mudar('cpf')} erro={erros.cpf} inputMode="numeric" autoComplete="off" placeholder="000.000.000-00" required />
        <CampoTexto rotulo="Endereço" opcional value={f.endereco} onChange={mudar('endereco')} erro={erros.endereco} autoComplete="street-address" maxLength={250}
          ajuda="Usado para sugerir o local ao registrar uma reclamação." />
        <CampoTexto rotulo="Senha" type="password" value={f.senha} onChange={mudar('senha')} erro={erros.senha} autoComplete="new-password" maxLength={72}
          ajuda="Entre 8 e 72 caracteres." required />
        <CampoTexto rotulo="Confirme a senha" type="password" value={f.confirmacao} onChange={mudar('confirmacao')} erro={erros.confirmacao} autoComplete="new-password" maxLength={72} required />
        <button type="submit" className="botao" disabled={enviando}>{enviando ? 'Criando cadastro…' : 'Criar cadastro'}</button>
      </form>
      <p className="troca">Já tem cadastro? <Link to="/entrar">Entrar</Link></p>
    </section>
  );
}
