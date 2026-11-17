import { Link } from 'react-router-dom';
export function NaoEncontrada() {
  return (
    <section className="cartao-estreito">
      <h1>Página não encontrada</h1>
      <p className="introducao">O endereço digitado não existe neste painel.</p>
      <Link to="/" className="botao">Ir para o início</Link>
    </section>
  );
}
