# Gestão de Custos

API REST para cadastro e consulta de despesas pessoais, desenvolvida com Java e Spring Boot. O projeto também inclui endpoints para experimentar paginação e cache em consultas de grandes volumes de dados.

## Funcionalidades

- Cadastro de despesas com validações de descrição, categoria, data e valor positivo.
- Consulta de todas as despesas salvas ou filtragem por e-mail e, opcionalmente, pela data do gasto.
- Exclusão de uma despesa pelo UUID.
- Resumo mensal por e-mail, filtrando pela data do gasto entre o primeiro e o último dia do mês e retornando o total calculado.
- Endpoints de desempenho para comparar consulta sem paginação, consulta paginada e consulta paginada com cache local.
- Banco H2 em arquivo para persistir os dados entre inicializações.
- Seeder de desempenho preparado no código, mas desativado por padrão para não inserir um grande lote de dados a cada inicialização.

## Tecnologias

- Java 26
- Spring Boot 4.2
- Spring Web MVC
- Spring Data JPA
- Spring Cache
- H2 Database
- Maven

## Executar localmente

É necessário ter o JDK 26 instalado. Na raiz do projeto — a pasta que contém `pom.xml` e `mvnw.cmd` — execute:

```powershell
.\mvnw.cmd spring-boot:run
```

O banco H2 é configurado em `src/main/resources/application.properties` com o caminho relativo `./data/gestao-despesa`. Execute a aplicação sempre a partir da raiz do projeto para que os dados sejam salvos na mesma pasta `data`.

Para executar os testes:

```powershell
.\mvnw.cmd test
```

## Endpoints da API

Base URL local: `http://localhost:8080`

### Despesas

| Método | Endpoint | Descrição |
|---|---|---|
| `POST`| `/gestao/create` | Cadastra uma despesa |
| `GET` | `/gestao/salvos` | Lista todas as despesas |
| `GET` | `/gestao/{email}`| Busca despesas pelo e-mail |
| `GET` | `/gestao/{email}?data=2026-11-20` | Busca despesas pelo e-mail e pela data do gasto |
| `DELETE` | `/gestao/remover/{id}` | Exclui uma despesa pelo UUID |
| `GET` | `/gestao/{email}/resumo-mensal?mes=2026-11` | Retorna despesas do mês e a soma dos valores |

Exemplo de corpo para cadastro:

```json
{
  "descricao": "Conta de internet",
  "data": "2026-11-15",
  "valor": 99.90,
  "categoria": "Moradia",
  "email": "usuario@email.com"
}
```

O endpoint de cadastro retorna a despesa criada, incluindo seu UUID. Use esse identificador na rota `DELETE`. A exclusão responde `204 No Content` quando remove o registro e `404 Not Found` quando o UUID não existe.

O resumo mensal recebe o mês no formato `yyyy-MM`, por exemplo:

```text
GET /gestao/usuario@email.com/resumo-mensal?mes=2026-11
```

A resposta contém o mês, as datas inicial e final do período, a lista de despesas e o campo `total`. A API entrega esses dados em JSON; a apresentação do total em uma última linha de tabela, com mesclagem de colunas e bordas, fica a cargo de uma futura interface gráfica.

### Desempenho

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/gestao/performance/sem-paginacao` | Lista todas as despesas sem paginação |
| `GET` | `/gestao/performance/com-paginacao?page=0&size=100` | Lista despesas em páginas |
| `GET` | `/gestao/performance/com-paginacao/{email}?page=0&size=100` | Busca por e-mail com paginação |
| `GET` | `/gestao/performance/cache/{email}?page=0&size=100` | Busca por e-mail com paginação e cache |

O cache simples é local à aplicação, vale apenas para a rota de desempenho anotada com `@Cacheable` e é perdido quando a aplicação reinicia. O cadastro e a exclusão limpam as entradas desse cache para evitar servir resultados antigos após alterações.

O seeder em `performance/GestaoDeDespesaSeeder.java` está desativado por padrão. Ativá-lo insere dados na despesas de teste no banco configurado; use um banco de teste ou faça backup antes de habilitá-lo.

## Organização do código

- `controller`: endpoints HTTP.
- `useCases`: regras de cadastro, busca, exclusão e resumo mensal.
- `repository`: consultas e persistência com Spring Data JPA.
- `model`: entidade persistida `Despesa`.
- `dto`: estruturas de resposta, como o resumo mensal.
- `performance`: endpoints e seeder voltados a testes de desempenho.

O projeto ainda não inclui interface gráfica nem autenticação de usuários. O H2 Console é uma ferramenta de administração do banco, não uma interface de uso final da aplicação.
