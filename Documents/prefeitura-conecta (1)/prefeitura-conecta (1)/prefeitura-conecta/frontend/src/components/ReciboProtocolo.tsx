import type { Protocolo } from '../api/types';
import { formatarData } from '../utils/format';
import { SeloStatus } from './SeloStatus';

/** Protocolo no formato de canhoto de atendimento, com todos os dados retornados pela API. */
export function ReciboProtocolo({ protocolo }: { protocolo: Protocolo }) {
  return (
    <article className="recibo" aria-labelledby={`recibo-${protocolo.id}`}>
      <header className="recibo-topo">
        <p className="recibo-legenda">Número do protocolo</p>
        <p className="recibo-codigo" id={`recibo-${protocolo.id}`}>{protocolo.codigo}</p>
        <SeloStatus status={protocolo.status} />
      </header>
      <div className="recibo-picote" aria-hidden="true" />
      <dl className="recibo-dados">
        <div><dt>Solicitante</dt><dd>{protocolo.nomeSolicitante}</dd></div>
        <div><dt>Registrado em</dt><dd>{formatarData(protocolo.criadoEm)}</dd></div>
        <div><dt>Área</dt><dd>{protocolo.categoria}</dd></div>
        <div><dt>Tipo de problema</dt><dd>{protocolo.tipoProblema}</dd></div>
        <div className="recibo-largo"><dt>Endereço da ocorrência</dt><dd>{protocolo.endereco}</dd></div>
        <div className="recibo-largo"><dt>Descrição</dt><dd className="recibo-descricao">{protocolo.descricao}</dd></div>
        <div className="recibo-rodape"><dt>Identificador interno</dt><dd>{protocolo.id}</dd></div>
      </dl>
    </article>
  );
}
