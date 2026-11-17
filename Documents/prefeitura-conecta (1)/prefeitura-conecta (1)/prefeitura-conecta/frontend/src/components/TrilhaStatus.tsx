import type { StatusProtocolo } from '../api/types';
import { ORDEM_STATUS, STATUS_INFO } from '../utils/format';

/** As três etapas do atendimento, em ordem, destacando a atual. É uma sequência real, por isso numerada. */
export function TrilhaStatus({ status }: { status: StatusProtocolo }) {
  const atual = ORDEM_STATUS.indexOf(status);
  return (
    <ol className="trilha" aria-label="Andamento do atendimento">
      {ORDEM_STATUS.map((s, i) => {
        const estado = i < atual ? 'feito' : i === atual ? 'atual' : 'pendente';
        return (
          <li key={s} className={`trilha-etapa trilha-${estado}`} aria-current={estado === 'atual' ? 'step' : undefined}>
            <span className="trilha-marca" aria-hidden="true">{i + 1}</span>
            <span className="trilha-texto">
              <strong>{STATUS_INFO[s].rotulo}</strong>
              {estado === 'atual' && <span>{STATUS_INFO[s].explicacao}</span>}
            </span>
          </li>
        );
      })}
    </ol>
  );
}
