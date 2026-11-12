# Prefeitura Conecta — API REST

Projeto inicial de API REST com Java 21, Spring Boot e Maven.

## Estrutura

- `controller`: recebe as requisições HTTP.
- `dto`: representa os dados recebidos na API.
- `resources`: configurações da aplicação.

## Executar no IntelliJ IDEA

1. Extraia o ZIP.
2. Abra a pasta `prefeitura-conecta` no IntelliJ IDEA.
3. Configure o Project SDK como JDK 21.
4. Aguarde o IntelliJ importar o `pom.xml` como projeto Maven e baixar as dependências.
5. Execute `PrefeituraConectaApplication`.

## Testar

Com a aplicação em execução, acesse:

`GET http://localhost:8080/reclamacoes/teste`

Para cadastrar uma reclamação, envie:

`POST http://localhost:8080/reclamacoes`

Header: `Content-Type: application/json`

Corpo JSON:

```json
{
  "titulo": "Buraco na rua",
  "descricao": "Existe um buraco grande na via.",
  "endereco": "Rua das Flores, 100",
  "categoria": "Infraestrutura"
}
```

A API retorna HTTP 201 (Created) quando recebe os dados.

**Importante:** nesta primeira versão, os dados são recebidos e validados, mas ainda não são salvos em banco de dados.
