import { useId, type InputHTMLAttributes, type ReactNode, type SelectHTMLAttributes, type TextareaHTMLAttributes } from 'react';

interface Base { rotulo: string; erro?: string; ajuda?: ReactNode; opcional?: boolean }

function Moldura({ id, rotulo, erro, ajuda, opcional, children }: Base & { id: string; children: ReactNode }) {
  return (
    <div className={`campo${erro ? ' campo-com-erro' : ''}`}>
      <label htmlFor={id}>
        {rotulo}{opcional && <span className="campo-opcional"> (opcional)</span>}
      </label>
      {children}
      {ajuda && !erro && <p className="campo-ajuda" id={`${id}-ajuda`}>{ajuda}</p>}
      {erro && <p className="campo-erro" id={`${id}-erro`}>{erro}</p>}
    </div>
  );
}

const descritoPor = (id: string, erro?: string, ajuda?: ReactNode) => (erro ? `${id}-erro` : ajuda ? `${id}-ajuda` : undefined);

export function CampoTexto({ rotulo, erro, ajuda, opcional, ...props }: Base & InputHTMLAttributes<HTMLInputElement>) {
  const id = useId();
  return (
    <Moldura id={id} rotulo={rotulo} erro={erro} ajuda={ajuda} opcional={opcional}>
      <input id={id} aria-invalid={!!erro} aria-describedby={descritoPor(id, erro, ajuda)} {...props} />
    </Moldura>
  );
}

export function CampoAreaTexto({ rotulo, erro, ajuda, opcional, ...props }: Base & TextareaHTMLAttributes<HTMLTextAreaElement>) {
  const id = useId();
  return (
    <Moldura id={id} rotulo={rotulo} erro={erro} ajuda={ajuda} opcional={opcional}>
      <textarea id={id} aria-invalid={!!erro} aria-describedby={descritoPor(id, erro, ajuda)} {...props} />
    </Moldura>
  );
}

export function CampoSelecao({ rotulo, erro, ajuda, opcional, children, ...props }: Base & SelectHTMLAttributes<HTMLSelectElement>) {
  const id = useId();
  return (
    <Moldura id={id} rotulo={rotulo} erro={erro} ajuda={ajuda} opcional={opcional}>
      <select id={id} aria-invalid={!!erro} aria-describedby={descritoPor(id, erro, ajuda)} {...props}>{children}</select>
    </Moldura>
  );
}
