import { useCallback, useEffect, useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ApiError } from '../api/client';
import { protocolosApi } from '../api/protocolos';
import type { Protocolo, StatusProtocolo } from '../api/types';
import { useAuth } from '../auth/AuthContext';
import { Aviso } from '../components/Aviso';
import { Carregando } from '../components/Carregando';
import { SeloStatus } from '../components/SeloStatus';
import { formatarData, ORDEM_STATUS, STATUS_INFO } from '../utils/format';

export function MeusProtocolos() {
  const { usuario } = useAuth();
  const navigate = useNavigate();
  const [lista, setLista] = useState<Protocolo[] | null>(null);
  const [erro, setErro] = useState<string | null>(null);
  const [atualizando, setAtualizando] = useState(false);
  const [filtro, setFiltro] = useState<StatusProtocolo | 'TODOS'>('TODOS');
  const [codigo, setCodigo] = useState('');
  const [erroBusca, setErroBusca] = useState<string | null>(null);
  const [buscando, setBuscando] = useState(false);

  const carregar = useCallback(async (signal?: AbortSignal) => {
    if (!usuario) return;
    setErro(null); setAtualizando(true);
    try {
      setLista(await protocolosApi.listarDoUsuario(usuario.id, signal));
    } catch (e) {
      if ((e as Error).name !== 'AbortError') setErro(e instanceof ApiError ? e.message : 'Não foi possível carregar seus protocolos.');
    } finally {
      setAtualizando(false);
    }
  }, [usuario]);

  useEffect(() => {
    const ctrl = new AbortController();
    carregar(ctrl.signal);
    return () => ctrl.abort();
  }, [carregar]);

  async function buscar(e: FormEvent) {
    e.preventDefault();
    setErroBusca(null);
    if (!codigo.trim()) { setErroBusca('Digite o número do protocolo.'); return; }
    setBuscando(true);
    try {
      const p = await protocolosApi.buscarPorCodigo(codigo);
      navigate(`/meus-protocolos/${p.id}`);
    } catch (err) {
      setErroBusca(err instanceof ApiError && err.status === 404
        ? 'Não há protocolo seu com esse número. Confira se digitou corretamente.'
        : err instanceof ApiError ? err.message : 'Não foi possível buscar.');
    } finally {
      setBuscando(false);
    }
  }

  const visiveis = (lista ?? []).filter((p) => filtro === 'TODOS' || p.status === filtro);
  const contagem = (s: StatusProtocolo) => (lista ?? []).filter((p) => p.status === s).length;

  return (
    <section className="pagina">
      <div className="cabecalho-pagina">
        <h1>Meus protocolos</h1>
        <button type="button" className="botao-secundario" onClick={() => carregar()} disabled={atualizando}>
          {atualizando ? 'Atualizando…' : 'Atualizar'}
        </button>
      </div>

      <form className="busca" onSubmit={buscar} role="search">
        <label htmlFor="busca-codigo">Buscar pelo número</label>
        <div className="busca-linha">
          <input id="busca-codigo" value={codigo} onChange={(e) => setCodigo(e.target.value)} placeholder="PC-20261115-A1B2C3D4"
            aria-describedby={erroBusca ? 'busca-erro' : undefined} autoComplete="off" spellCheck={false} />
          <button type="submit" className="botao" disabled={buscando}>{buscando ? 'Buscando…' : 'Buscar'}</button>
        </div>
        {erroBusca && <p className="campo-erro" id="busca-erro">{erroBusca}</p>}
      </form>

      {erro && <Aviso tipo="erro">{erro}</Aviso>}
      {!lista && !erro && <Carregando texto="Carregando seus protocolos…" />}

      {lista && lista.length === 0 && (
        <div className="vazio">
          <p>Você ainda não registrou nenhuma reclamação.</p>
          <Link to="/nova-reclamacao" className="botao">Registrar reclamação</Link>
        </div>
      )}

      {lista && lista.length > 0 && (
        <>
          <div className="filtros" role="group" aria-label="Filtrar por status">
            <button type="button" aria-pressed={filtro === 'TODOS'} onClick={() => setFiltro('TODOS')}>Todos ({lista.length})</button>
            {ORDEM_STATUS.map((s) => (
              <button key={s} type="button" aria-pressed={filtro === s} onClick={() => setFiltro(s)}>
                {STATUS_INFO[s].rotulo} ({contagem(s)})
              </button>
            ))}
          </div>
          {visiveis.length === 0
            ? <p className="sem-resultado">Nenhum protocolo com esse status.</p>
            : (
              <ul className="lista-protocolos">
                {visiveis.map((p) => (
                  <li key={p.id}>
                    <Link to={`/meus-protocolos/${p.id}`} className="linha-protocolo">
                      <span className="linha-codigo">{p.codigo}</span>
                      <span className="linha-tipo">
                        <strong>{p.tipoProblema}</strong>
                        <span>{p.categoria}</span>
                      </span>
                      <span className="linha-data">{formatarData(p.criadoEm, false)}</span>
                      <SeloStatus status={p.status} />
                    </Link>
                  </li>
                ))}
              </ul>
            )}
        </>
      )}
    </section>
  );
}
