import type { StatusProtocolo } from '../api/types';
import { rotuloStatus } from '../utils/format';

/** Exibe o status (somente leitura — o cidadão não altera status). */
export function SeloStatus({ status }: { status: StatusProtocolo }) {
  return <span className={`selo selo-${status.toLowerCase()}`}>{rotuloStatus(status)}</span>;
}
