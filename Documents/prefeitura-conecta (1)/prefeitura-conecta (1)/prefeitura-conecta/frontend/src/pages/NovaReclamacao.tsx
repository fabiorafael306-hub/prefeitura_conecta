import { useEffect, useMemo, useRef, useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { agruparPorArea, catalogoApi } from '../api/catalogo';
import { ApiError } from '../api/client';
import { protocolosApi } from '../api/protocolos';
import type { Categoria, Protocolo } from '../api/types';
import { useAuth } from '../auth/AuthContext';
import { CampoAreaTexto, CampoSelecao, CampoTexto } from '../components/Campo';
import { Aviso } from '../components/Aviso';
import { Carregando } from '../components/Carregando';
import { ReciboProtocolo } from '../components/ReciboProtocolo';

const MAX_DESCRICAO = 5000;
type Campos = 'area' | 'categoriaId' | 'descricao' | 'endereco';

export function NovaReclamacao() {
  const { usuario } = useAuth();
  const [categorias, setCategorias] = useState<Categoria[] | null>(null);
  const [erroCatalogo, setErroCatalogo] = useState<string | null>(null);
  const [tentativa, setTentativa] = useState(0);

  const [area, setArea] = useState('');
  const [categoriaId, setCategoriaId] = useState('');
  const [descricao, setDescricao] = useState('');
  const [endereco, setEndereco] = useState('');
  const [erros, setErros] = useState<Partial<Record<Campos, string>>>({});
  const [erroGeral, setErroGeral] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const [protocolo, setProtocolo] = useState<Protocolo | null>(null);
  const tituloResultado = useRef<HTMLHeadingElement>(null);

  // Catálogo carregado do backend sempre que a tela abre (nada fixo no frontend).
  useEffect(() => {
    const ctrl = new AbortController();
    setErroCatalogo(null); setCategorias(null);
    catalogoApi.listar(ctrl.signal)
      .then(setCategorias)
      .catch((e) => { if (e.name !== 'AbortError') setErroCatalogo(e instanceof ApiError ? e.message : 'Não foi possível carregar as opções.'); });
    return () => ctrl.abort();
  }, [tentativa]);

  const areas = useMemo(() => agruparPorArea(categorias ?? []), [categorias]);
  const tiposDaArea = areas.find((a) => a.codigoArea === area)?.tipos ?? [];

  useEffect(() => { if (protocolo) tituloResultado.current?.focus(); }, [protocolo]);

  async function enviar(e: FormEvent) {
    e.preventDefault();
    setErroGeral(null);
    const locais: Partial<Record<Campos, string>> = {};
    if (!area) locais.area = 'Escolha a área.';
    if (!categoriaId) locais.categoriaId = 'Escolha o tipo de problema.';
    if (!descricao.trim()) locais.descricao = 'Descreva o problema.';
    if (!endereco.trim()) locais.endereco = 'Informe onde está o problema.';
    setErros(locais);
    if (Object.keys(locais).length) return;

    setEnviando(true);
    try {
      const p = await protocolosApi.registrarReclamacao({
        categoriaId: Number(categoriaId), descricao: descricao.trim(), endereco: endereco.trim(),
      });
      setProtocolo(p);
    } catch (err) {
      if (err instanceof ApiError) {
        setErros(err.campos as Partial<Record<Campos, string>>);
        setErroGeral(err.message);
      } else setErroGeral('Não foi possível registrar a reclamação.');
    } finally {
      setEnviando(false);
    }
  }

  function novaReclamacao() {
    setProtocolo(null); setArea(''); setCategoriaId(''); setDescricao(''); setEndereco(''); setErros({});
  }

  if (protocolo) {
    return (
      <section className="pagina">
        <h1 ref={tituloResultado} tabIndex={-1}>Reclamação registrada</h1>
        <p className="introducao">Guarde o número do protocolo para acompanhar o atendimento.</p>
        <ReciboProtocolo protocolo={protocolo} />
        <div className="acoes">
          <Link to={`/meus-protocolos/${protocolo.id}`} className="botao">Acompanhar este protocolo</Link>
          <button type="button" className="botao-secundario" onClick={novaReclamacao}>Registrar outra reclamação</button>
        </div>
      </section>
    );
  }

  return (
    <section className="pagina">
      <h1>Nova reclamação</h1>
      <p className="introducao">Escolha o tipo de problema, descreva o que está acontecendo e informe o local.</p>

      {erroCatalogo && (
        <Aviso tipo="erro">
          {erroCatalogo}{' '}
          <button type="button" className="link-botao" onClick={() => setTentativa((t) => t + 1)}>Tentar de novo</button>
        </Aviso>
      )}
      {!categorias && !erroCatalogo && <Carregando texto="Carregando tipos de problema…" />}
      {categorias && categorias.length === 0 && <Aviso>Nenhum tipo de problema está disponível no momento.</Aviso>}

      {categorias && categorias.length > 0 && (
        <form onSubmit={enviar} noValidate className="formulario">
          {erroGeral && <Aviso tipo="erro">{erroGeral}</Aviso>}
          <div className="grade-2">
            <CampoSelecao rotulo="Área" value={area} erro={erros.area} required
              onChange={(e) => { setArea(e.target.value); setCategoriaId(''); setErros((x) => ({ ...x, area: undefined })); }}>
              <option value="">Selecione</option>
              {areas.map((a) => <option key={a.codigoArea} value={a.codigoArea}>{a.nomeArea}</option>)}
            </CampoSelecao>
            <CampoSelecao rotulo="Tipo de problema" value={categoriaId} erro={erros.categoriaId} required disabled={!area}
              ajuda={!area ? 'Escolha a área primeiro.' : undefined}
              onChange={(e) => { setCategoriaId(e.target.value); setErros((x) => ({ ...x, categoriaId: undefined })); }}>
              <option value="">Selecione</option>
              {tiposDaArea.map((t) => <option key={t.id} value={t.id}>{t.nomeTipo}</option>)}
            </CampoSelecao>
          </div>
          <CampoAreaTexto rotulo="Descrição" value={descricao} erro={erros.descricao} rows={5} maxLength={MAX_DESCRICAO} required
            placeholder="Ex.: buraco grande perto da faixa de pedestres, já causou acidente com moto."
            ajuda={`${descricao.length} de ${MAX_DESCRICAO} caracteres`}
            onChange={(e) => { setDescricao(e.target.value); setErros((x) => ({ ...x, descricao: undefined })); }} />
          <CampoTexto rotulo="Endereço da ocorrência" value={endereco} erro={erros.endereco} maxLength={250} required
            placeholder="Rua, número aproximado, bairro e um ponto de referência"
            onChange={(e) => { setEndereco(e.target.value); setErros((x) => ({ ...x, endereco: undefined })); }} />
          {usuario?.endereco && !endereco && (
            <button type="button" className="link-botao" onClick={() => setEndereco(usuario.endereco ?? '')}>
              Usar meu endereço de cadastro
            </button>
          )}
          <div className="acoes">
            <button type="submit" className="botao" disabled={enviando}>{enviando ? 'Registrando…' : 'Registrar reclamação'}</button>
          </div>
        </form>
      )}
    </section>
  );
}
