# BioCompiler 1.0

O BioCompiler e uma aplicacao web para analise de sequencias de DNA. Ele combina uma interface visual para estudantes e pesquisadores de Biologia com uma API REST e persistencia local para quem desenvolve, testa ou estende o sistema.

## Para quem trabalha com Biologia

Uma sequencia de DNA e analisada em trincas de nucleotideos, chamadas codons. O sistema procura principalmente:

- **Start codon**: `ATG`, ponto de inicio da regiao codificante.
- **Stop codon**: `TAA`, `TAG` ou `TGA`, sinais de termino no mesmo quadro de leitura.
- **Quadro de leitura**: a posicao inicial usada para separar a sequencia em codons.
- **Bases invalidas**: caracteres diferentes de `A`, `T`, `C` e `G`.
- **Frameshift**: sequencias cuja quantidade de bases altera a separacao em trincas.
- **Nonsense mutation**: presenca de um codon de parada prematuro durante a analise.

A pagina de analise apresenta a sequencia, o pre-mRNA e a visualizacao dos codons. A pagina de historico mostra os resultados salvos, estatisticas por tipo e paginacao. Cada navegador possui uma sessao propria para que os historicos nao se misturem.

## Para quem trabalha com Computacao

O projeto e dividido em tres servicos:

- **Frontend**: React, TypeScript, Vite e TanStack Router. Porta `5173`.
- **Backend**: Spring Boot, Java 25, Spring Web MVC, Spring Data JPA e Hibernate. Porta `8080`.
- **Banco**: SQLite persistido em um volume Docker compartilhado.

O backend recebe a sequencia, executa as regras de analise, salva o resultado e oferece endpoints para consulta detalhada, historico, estatisticas e limpeza do historico. O `sessionId` e gerado no frontend e armazenado em `sessionStorage`, sendo enviado nas requisicoes relacionadas ao historico.

## Estrutura

```text
backend/biocompiler/       API Spring Boot e testes
frontend/biocompiler/      Interface React/TypeScript
database/sqlite/            Imagem do servico SQLite
docker-compose.yml         Orquestracao dos servicos
docs/                      Documentacao complementar
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

O banco fica no volume `biocompiler_sqlite-data`. Para remover tambem os dados persistidos:

```bash
docker compose down -v
```

## Deploy no Render

O frontend pode ser criado no Render como um **Web Service** usando ambiente Docker:

1. Selecione este repositorio e defina o caminho do Dockerfile como `frontend/biocompiler/Dockerfile`.
2. Defina o diretorio de contexto como `frontend/biocompiler`.
3. Configure `VITE_API_BASE_URL` com a URL publica do backend, por exemplo `https://biocompiler-api.onrender.com`.
4. Garanta que essa variavel esteja disponivel durante o build, pois o Vite incorpora valores `VITE_*` no bundle.
5. Use a porta fornecida pelo Render. O Dockerfile utiliza automaticamente `PORT` e assume `10000` como valor local.

O backend deve ser publicado como outro **Web Service** Docker usando `backend/biocompiler/Dockerfile`. No Render, configure exatamente a variavel `SPRING_DATASOURCE_URL` com o valor `jdbc:sqlite:/app/data/biocompiler.db`; nao informe somente `/app/data/biocompiler.db`. Adicione um Persistent Disk montado em `/app/data`. O servico SQLite do Compose serve para o ambiente local; no Render, o arquivo SQLite deve ficar no disco persistente do backend.

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
