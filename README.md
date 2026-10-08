# BioCompiler 1.0

Aplicação web e terminal para análise de sequências de DNA. O BioCompiler identifica códons de início e parada, bases inválidas, alteração de quadro de leitura (*frameshift*) e mutações *nonsense*, além de transcrever sequências válidas para pré-mRNA.

## Funcionalidades

- Análise individual de sequências de DNA pela interface web ou terminal.
- Importação de arquivos `.txt` e `.csv`, com uma sequência por linha.
- Histórico por navegador, estatísticas, paginação, exportação e limpeza do histórico.
- API REST Spring Boot e banco PostgreSQL.
- Modo interativo de terminal para análise individual ou em lote.

O histórico é separado por um `sessionId` UUID salvo no `localStorage` do navegador. Ele continua disponível após fechar e reabrir o navegador no mesmo dispositivo, mas não é compartilhado entre navegadores, dispositivos ou perfis anônimos.

## Arquitetura

```text
frontend/biocompiler/      React, TypeScript, Vite e TanStack Router
backend/biocompiler/       API Spring Boot, JPA e CLI
docker-compose.yml          Frontend, backend e PostgreSQL locais
render.yaml                 Blueprint dos serviços web no Render (banco externo no Neon)
```

| Componente | Tecnologia | Porta local |
| --- | --- | --- |
| Frontend | React 19, Vite | 5173 |
| Backend | Java 25, Spring Boot | 8080 |
| Banco | PostgreSQL 17 | 5433 (host) / 5432 (container) |

## Requisitos

### Com Docker (recomendado)

- Docker Desktop com engine Linux ativo.
- Docker Compose v2.

### Sem Docker

- Java 25.
- Node.js 22 ou superior e npm.
- PostgreSQL 17 acessível localmente ou em rede.

## Executar com Docker Compose

1. Crie o arquivo de ambiente a partir do exemplo:

   ```powershell
   Copy-Item .env-example .env
   ```

   Em macOS/Linux:

   ```bash
   cp .env-example .env
   ```

2. Ajuste as credenciais no `.env` se necessário. Não versione esse arquivo.

3. Suba todos os serviços na raiz do repositório:

   ```bash
   docker compose up -d --build
   ```

4. Acesse:

   - Frontend: <http://localhost:5173>
   - API: <http://localhost:8080>
   - Health check: <http://localhost:8080/api/health>

Comandos úteis:

```bash
docker compose ps
docker compose logs -f frontend
docker compose logs -f backend
docker compose down
```

`docker compose down` preserva o banco no volume `biocompiler_postgres-data`. Para apagar deliberadamente todos os dados locais, use `docker compose down -v`.

## Executar sem Docker

### 1. Inicie PostgreSQL

Crie um banco e um usuário PostgreSQL com as credenciais desejadas. Como alternativa, inicie somente o banco pelo Compose:

```bash
docker compose up -d postgres
```

Com os valores padrão de `.env-example`, ele estará em `localhost:5433`.

### 2. Inicie o backend

No PowerShell, na raiz do projeto, defina a conexão com o banco:

```powershell
$env:DATABASE_HOST = "localhost"
$env:DATABASE_PORT = "5433"
$env:DATABASE_NAME = "biocompiler"
$env:DATABASE_USER = "biocompiler"
$env:DATABASE_PASSWORD = "change-this-password"
```

Depois execute:

```powershell
cd backend/biocompiler
.\mvnw.cmd spring-boot:run
```

Em macOS/Linux:

```bash
cd backend/biocompiler
DATABASE_HOST=localhost DATABASE_PORT=5433 DATABASE_NAME=biocompiler \
DATABASE_USER=biocompiler DATABASE_PASSWORD=change-this-password \
./mvnw spring-boot:run
```

O Hibernate cria e atualiza o esquema automaticamente (`spring.jpa.hibernate.ddl-auto=update`).

### 3. Inicie o frontend

Em outro terminal:

```powershell
cd frontend/biocompiler
npm install
$env:VITE_API_BASE_URL = "http://localhost:8080"
npm run dev
```

Em macOS/Linux:

```bash
cd frontend/biocompiler
npm install
VITE_API_BASE_URL=http://localhost:8080 npm run dev
```

Abra <http://localhost:5173>. `VITE_API_BASE_URL` é incorporada no build de produção; alterá-la exige reconstruir o frontend.

## Modo terminal do backend

O perfil `terminal` inicia a CLI e não sobe o servidor HTTP. A CLI ainda inicializa o contexto Spring e, portanto, precisa das mesmas variáveis de conexão com PostgreSQL descritas acima.

No PowerShell:

```powershell
cd backend/biocompiler
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=terminal"
```

Em macOS/Linux:

```bash
cd backend/biocompiler
./mvnw spring-boot:run -Dspring-boot.run.profiles=terminal
```

O menu oferece:

1. Analisar uma sequência digitada.
2. Analisar um arquivo em lote e, opcionalmente, exportar o resultado.
0. Encerrar o programa.

No processamento em lote, a exportação padrão é `resultados.txt` no diretório atual, em formato separado por ponto e vírgula. A CLI analisa em memória: os resultados dela não são adicionados ao histórico da aplicação web.

## Formato de entrada

Use apenas as bases `A`, `T`, `C` e `G`. O leitor aceita `.txt` e `.csv`, uma sequência por linha, ignora linhas vazias e ignora o cabeçalho `entrada` ou `.entrada`.

```text
ATGGCCATTGTAATGGGCCGCTGAAAGGGTGCCCGATAG
ATGAAACCCGGGTTTTAA
```

## API REST

Base: `/api/analysis`

| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/` | Analisa uma sequência JSON (`sequence`, `sessionId`) |
| `POST` | `/file` | Analisa arquivo multipart (`file`, `sessionId`) |
| `GET` | `/{id}` | Busca uma análise pelo ID |
| `GET` | `/history` | Histórico paginado da sessão |
| `GET` | `/history/stats` | Estatísticas da sessão |
| `GET` | `/history/export` | Exporta o histórico da sessão |
| `DELETE` | `/history` | Remove o histórico da sessão |

As rotas de histórico usam `sessionId` como query parameter UUID. Exemplos:

```bash
curl -X POST http://localhost:8080/api/analysis \
  -H "Content-Type: application/json" \
  -d '{"sequence":"ATGAAACCCTGA","sessionId":"00000000-0000-0000-0000-000000000001"}'

curl "http://localhost:8080/api/analysis/history?page=0&size=8&sessionId=00000000-0000-0000-0000-000000000001"
```

## Testes e build

Backend:

```powershell
cd backend/biocompiler
.\mvnw.cmd test
.\mvnw.cmd package
```

Frontend:

```powershell
cd frontend/biocompiler
npm install
npm run lint
npm run build
```

## Deploy no Render com PostgreSQL Neon

O [render.yaml](render.yaml) cria os dois serviços web no Render:

- `biocompiler-api`: API Spring Boot;
- `biocompiler-web`: frontend TanStack Start.

O PostgreSQL é externo e deve ser criado no [Neon](https://neon.com/). Escolha uma região próxima de Oregon, a região configurada para os serviços Render. Não é necessário importar dados para um deploy novo.

### 1. Criar e obter a conexão do Neon

No painel do Neon, crie o projeto e selecione a opção de conexão **Pooled connection**. O Neon apresenta uma URL semelhante a:

```text
postgresql://neondb_owner:SENHA@ep-exemplo-pooler.us-west-2.aws.neon.tech/neondb?sslmode=require
```

Para o Spring Boot, altere somente o protocolo para o formato JDBC:

```text
jdbc:postgresql://ep-exemplo-pooler.us-west-2.aws.neon.tech/neondb?sslmode=require
```

O usuário e a senha ficam em variáveis separadas. Não versione credenciais reais.

### 2. Configurar o Blueprint e os segredos

No Render, crie ou sincronize o Blueprint deste repositório. Configure no serviço `biocompiler-api`:

| Variável | Valor |
| --- | --- |
| `SPRING_DATASOURCE_URL` | URL JDBC pooled do Neon com `sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` | Usuário informado pelo Neon |
| `SPRING_DATASOURCE_PASSWORD` | Senha informada pelo Neon |

Essas variáveis estão marcadas como `sync: false` no Blueprint. Em um Blueprint novo, o Render solicita os valores durante a criação. Em um Blueprint que já existe, cadastre ou atualize os valores manualmente em **biocompiler-api > Environment**.

As demais ligações continuam automáticas:

- `CORS_ALLOWED_ORIGINS` recebe a URL pública de `biocompiler-web`;
- `VITE_API_BASE_URL` recebe a URL pública de `biocompiler-api` durante o build do frontend.

### 3. Publicar e validar

1. Publique primeiro `biocompiler-api`.
2. Confirme que `https://<api>.onrender.com/api/health` responde com sucesso.
3. No Neon, confirme que a tabela `analyses` foi criada.
4. Publique `biocompiler-web`.
5. Crie uma análise, reinicie a API e confirme que o histórico continua disponível.

O perfil de produção usa `spring.jpa.hibernate.ddl-auto=update`; portanto, reiniciar ou republicar a API não remove os registros. O sistema não depende do filesystem efêmero dos containers.

## Solução de problemas

- **Frontend não alcança a API:** confirme `VITE_API_BASE_URL=http://localhost:8080` em desenvolvimento e que o backend está em execução.
- **Erro de conexão com o banco:** confirme host, porta, banco, usuário e senha. Com PostgreSQL via Compose, use `localhost:5433` ao executar serviços diretamente na máquina.
- **Erro de conexão com o Neon:** confirme que `SPRING_DATASOURCE_URL` começa com `jdbc:postgresql://`, usa o host com sufixo `-pooler` e termina com `sslmode=require`; confira também usuário e senha separados.
- **Blueprint existente não recebeu os segredos:** variáveis `sync: false` novas devem ser preenchidas manualmente no painel do serviço.
- **Histórico vazio:** confirme que está no mesmo navegador/perfil; o histórico é filtrado pelo `sessionId` salvo no `localStorage`.
- **Porta ocupada:** altere `BACKEND_PORT`, `FRONTEND_PORT` ou `POSTGRES_PORT` no `.env` e reinicie o Compose.
