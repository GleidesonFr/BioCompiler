# BioCompiler 1.0

O BioCompiler e uma aplicacao web e terminal para analise de sequencias de DNA. Ele combina uma interface visual para estudantes e pesquisadores de Biologia com uma API REST, persistencia local e uma CLI para uso em laboratorio, aulas e testes automatizados.

## Para quem trabalha com Biologia

Uma sequencia de DNA e analisada em trincas de nucleotideos, chamadas codons. O sistema procura principalmente:

- **Start codon**: `ATG`, ponto de inicio da regiao codificante.
- **Stop codon**: `TAA`, `TAG` ou `TGA`, sinais de termino no mesmo quadro de leitura.
- **Quadro de leitura**: a posicao inicial usada para separar a sequencia em codons.
- **Bases invalidas**: caracteres diferentes de `A`, `T`, `C` e `G`.
- **Frameshift**: sequencias cuja quantidade de bases altera a separacao em trincas.
- **Nonsense mutation**: presenca de um codon de parada prematuro durante a analise.

A pagina de analise apresenta a sequencia, o pre-mRNA e a visualizacao dos codons com animacoes da leitura e da transcricao. A pagina de historico mostra os resultados salvos, estatisticas por tipo, paginacao, carregamento restrito a tabela e limpeza do historico. Cada navegador possui uma sessao propria para que os historicos nao se misturem.

## Para quem trabalha com Computacao

O projeto e dividido em tres servicos:

- **Frontend**: React, TypeScript, Vite e TanStack Router. Porta `5173`.
- **Backend**: Spring Boot, Java 25, Spring Web MVC, Spring Data JPA e Hibernate. Porta `8080`.
- **Banco**: PostgreSQL em container no ambiente local e configurável por variáveis de ambiente no deploy.

O backend recebe a sequencia, executa as regras de analise, salva o resultado e oferece endpoints para consulta detalhada, historico, estatisticas e limpeza do historico. O `sessionId` e gerado no frontend e armazenado em `sessionStorage`, sendo enviado nas requisicoes relacionadas ao historico.

## Estrutura

```text
backend/biocompiler/       API Spring Boot e testes
frontend/biocompiler/      Interface React/TypeScript
database/sqlite/            Artefatos legados do banco local
docker-compose.yml          Orquestracao dos servicos
frontend/INTEGRACAO_*.md    Documentacao de integracao da API
```

## Requisitos

Para executar localmente:

- Java 25
- Node.js 22 ou superior
- npm
- SQLite opcional para inspeção manual do banco

Para executar com containers:

- Docker Desktop com engine Linux ativo
- Docker Compose v2

## Executar com Docker Compose

Na raiz do projeto:

```bash
docker compose up -d --build
```

Acesse:

- Frontend: http://localhost:5173
- Backend: http://localhost:8080

Verifique os servicos com:

```bash
docker compose ps
docker compose logs -f backend
```

Para parar os containers sem apagar os dados:

```bash
docker compose down
```

O banco fica no volume `biocompiler_postgres-data`. Para remover tambem os dados persistidos:

```bash
docker compose down -v
```

O Compose usa a porta `8080` para o backend e `5173` para o frontend. O primeiro acesso pode demorar enquanto as imagens sao construidas e o Spring Boot inicializa.

## Deploy no Render

O repositório inclui um Blueprint em [render.yaml](render.yaml), que cria três recursos na mesma região:

- `biocompiler-api`: backend Spring Boot como Web Service;
- `biocompiler-web`: frontend como Web Service;
- `biocompiler-db`: PostgreSQL gerenciado.

No Render, selecione **New > Blueprint**, conecte o repositório e confirme o arquivo `render.yaml`. O Blueprint conecta o backend ao PostgreSQL por rede privada e injeta a URL pública da API no build do frontend. Também configura o CORS do backend com a URL pública do frontend.

Os nomes dos serviços precisam ser únicos no seu workspace. Caso já existam recursos com esses nomes, altere os três nomes e as respectivas referências `name` no mesmo arquivo antes da primeira sincronização.

O Render fornece a variável `PORT` para cada Web Service; os Dockerfiles usam essa porta automaticamente. `VITE_API_BASE_URL` é incorporada no bundle durante o build, portanto qualquer alteração dessa variável exige um novo deploy do frontend.

## Executar sem Docker

### Backend

```bash
cd backend/biocompiler
./mvnw spring-boot:run
```

No Windows PowerShell:

```powershell
cd backend/biocompiler
.\mvnw.cmd spring-boot:run
```

O banco local sera criado em `backend/biocompiler/data/biocompiler.db`.

### Frontend

Em outro terminal:

```bash
cd frontend/biocompiler
npm install
npm run dev
```

Por padrao, o frontend usa a mesma origem para a API quando `VITE_API_BASE_URL` nao esta definido. Para apontar explicitamente para o backend local:

```bash
VITE_API_BASE_URL=http://localhost:8080 npm run dev
```

No PowerShell:

```powershell
$env:VITE_API_BASE_URL = "http://localhost:8080"
npm run dev
```

### CLI do backend

O backend tambem possui um modo interativo para analise pelo terminal. Ele e ativado pelo profile `terminal` e nao inicia o servidor web:

```powershell
cd backend/biocompiler
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=terminal"
```

No menu da CLI, e possivel analisar uma sequencia individual, processar um arquivo em lote, exibir o resumo por tipo de resultado e exportar os resultados para um arquivo delimitado por ponto e virgula, por padrao `resultados.txt`.

O arquivo de entrada da CLI aceita uma sequencia por linha e pode ser usado com `.txt` ou `.csv`. A linha `entrada` ou `.entrada` e tratada como cabecalho e ignorada.

## Formatos de entrada

A interface aceita sequencias digitadas ou arquivos `.csv` e `.txt`. Nos arquivos, cada linha deve conter uma sequencia de DNA. Linhas vazias e cabecalhos devem ser evitados. O backend remove espacos laterais e ignora a linha de cabecalho `entrada` ou `.entrada`.

Exemplo:

```text
ATGGCCATTGTAATGGGCCGCTGAAAGGGTGCCCGATAG
ATGAAACCCGGGTTTTAA
```

## API principal

Todas as rotas ficam sob `/api/analysis`:

| Metodo | Rota | Funcao |
| --- | --- | --- |
| `POST` | `/api/analysis` | Analisa uma sequencia enviada em JSON |
| `POST` | `/api/analysis/file` | Analisa sequencias de um arquivo multipart |
| `GET` | `/api/analysis/{id}` | Retorna uma analise especifica |
| `GET` | `/api/analysis/history` | Lista o historico paginado por sessao |
| `GET` | `/api/analysis/history/stats` | Retorna os totais por tipo de resultado |
| `DELETE` | `/api/analysis/history` | Limpa o historico da sessao |

As rotas de historico exigem o parametro `sessionId` como UUID. Consulte [frontend/INTEGRACAO_BACKEND_BIOCOMPILER.md](frontend/INTEGRACAO_BACKEND_BIOCOMPILER.md) para detalhes adicionais de integracao.

## Testes e build

Backend:

```powershell
cd backend/biocompiler
.\mvnw.cmd test
```

Frontend:

```powershell
cd frontend/biocompiler
npm.cmd run build
npm.cmd run lint
```

## Contribuindo

Ao alterar as regras biologicas, atualize os testes dos servicos correspondentes e descreva no pull request qual regra de codons ou quadro de leitura foi modificada. Ao alterar o contrato HTTP, atualize a API do frontend, os testes do backend e a documentacao.
