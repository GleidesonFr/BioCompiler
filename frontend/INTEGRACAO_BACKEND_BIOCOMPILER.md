# Integração Lovable ↔ BioCompiler

O frontend foi adaptado para consumir o backend Spring Boot como fonte de verdade.

## Endpoints esperados

- `POST /api/analysis` — recebe `{ "sequence": "ATG..." }` e retorna uma análise.
- `POST /api/analysis/file` — recebe `multipart/form-data` com campo `file` e retorna uma lista de análises.
- `GET /api/analysis/history` — retorna todas as análises persistidas.
- `GET /api/analysis/{id}` — retorna uma análise persistida pelo UUID.
- `DELETE /api/analysis/history` — remove o histórico inteiro.

## Contrato de resposta

O frontend usa os nomes do modelo `Analysis` do backend:

```json
{
  "id": "uuid",
  "originalSequence": "ATG...",
  "resultType": "CORRECT",
  "positionStart": 0,
  "positionStop": 12,
  "readingFrame": "FRAME_0",
  "codingRegion": "ATG...TAA",
  "preMrna": "AUG...UAA",
  "message": "CORRETO",
  "analysisDate": "2026-08-27T15:30:00"
}
```

`resultType` aceitos pelo frontend:

- `CORRECT`
- `INVALID_BASE`
- `START_CODON_NOT_FOUND`
- `STOP_CODON_NOT_FOUND`
- `FRAME_SHIFT`
- `NONSENSE_MUTATION`

## Mudanças importantes no frontend

1. A análise deixou de ser feita em `src/lib/dna.ts`.
2. `src/lib/api.ts` agora concentra as chamadas HTTP.
3. O histórico deixou de usar `localStorage` e passou a usar o banco do Spring Boot.
4. A página de detalhe busca a análise pelo UUID no backend.
5. O upload agora aceita CSV, conforme o trabalho.
6. A interface visual do Lovable foi preservada.
7. `VITE_API_BASE_URL` pode ser definido quando frontend e backend estiverem em origens diferentes. Em mesma origem, pode ficar vazio.

## CORS

Se o Vite/Lovable estiver rodando em uma porta/origem diferente do Spring Boot, o backend precisa liberar a origem do frontend. Em produção, liberar somente a origem real do frontend.
