import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ApiError } from '../api/client';
import { protocolosApi } from '../api/protocolos';
import type { Protocolo } from '../api/types';
import { Aviso } from '../components/Aviso';
import { Carregando } from '../components/Carregando';
import { ReciboProtocolo } from '../components/ReciboProtocolo';
import { TrilhaStatus } from '../components/TrilhaStatus';

export function DetalheProtocolo() {
  const { id } = useParams();
  const idNumerico = Number(id);
  const [protocolo, setProtocolo] = useState<Protocolo | null>(null);
  const [erro, setErro] = useState<string | null>(null);
  const [atualizando, setAtualizando] = useState(false);

  const carregar = useCallback(async (signal?: AbortSignal) => {
    if (!Number.isInteger(idNumerico) || idNumerico <= 0) { setErro('Endereço de protocolo inválido.'); return; }
    setErro(null); setAtualizando(true);
    try {
      setProtocolo(await protocolosApi.buscarPorId(idNumerico, signal));
    } catch (e) {
      if ((e as Error).name === 'AbortError') return;
      // 404 também cobre protocolos de outros cidadãos: o backend não revela se existem.
      setErro(e instanceof ApiError && e.status === 404 ? 'Protocolo não encontrado entre os seus.' : e instanceof ApiError ? e.message : 'Não foi possível carregar o protocolo.');
    } finally {
      setAtualizando(false);
    }
  }, [idNumerico]);

  useEffect(() => {
    const ctrl = new AbortController();
    carregar(ctrl.signal);
    return () => ctrl.abort();
  }, [carregar]);

  return (
    <section className="pagina">
      <p className="voltar"><Link to="/meus-protocolos">Voltar para Meus protocolos</Link></p>
      {erro && <Aviso tipo="erro">{erro}</Aviso>}
      {!protocolo && !erro && <Carregando texto="Carregando protocolo…" />}
      {protocolo && (
        <>
          <div className="cabecalho-pagina">
            <h1>{protocolo.tipoProblema}</h1>
            <button type="button" className="botao-secundario" onClick={() => carregar()} disabled={atualizando}>
              {atualizando ? 'Atualizando…' : 'Atualizar status'}
            </button>
          </div>
          <div className="detalhe-grade">
            <ReciboProtocolo protocolo={protocolo} />
            <aside className="detalhe-lateral">
              <h2>Andamento</h2>
              <TrilhaStatus status={protocolo.status} />
              <p className="nota">O status é atualizado pela prefeitura. Volte aqui para acompanhar.</p>
            </aside>
          </div>
        </>
      )}
    </section>
  );
}
