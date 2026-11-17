export function Carregando({ texto = 'Carregando…' }: { texto?: string }) {
  return (
    <div className="carregando" role="status" aria-live="polite">
      <span className="carregando-ponto" aria-hidden="true" />
      {texto}
    </div>
  );
}
