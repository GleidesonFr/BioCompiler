export type AnalysisStatus =
  | "ok"
  | "invalid_base"
  | "start_missing"
  | "stop_missing"
  | "frame_shift"
  | "nonsense";

export type Severity = "aprovado" | "alerta" | "erro";

export const STATUS_META: Record<
  AnalysisStatus,
  { label: string; short: string; tone: string; severity: Severity }
> = {
  ok: { label: "Sequência válida", short: "Correto", tone: "ok", severity: "aprovado" },
  invalid_base: { label: "Base inválida", short: "Base inválida", tone: "bad", severity: "erro" },
  start_missing: { label: "START ausente", short: "Sem START", tone: "bad", severity: "erro" },
  stop_missing: { label: "STOP ausente", short: "Sem STOP", tone: "bad", severity: "erro" },
  frame_shift: { label: "Frameshift", short: "Frameshift", tone: "warn", severity: "alerta" },
  nonsense: { label: "Nonsense mutation", short: "Nonsense", tone: "warn", severity: "alerta" },
};

export const STATUS_ORDER: AnalysisStatus[] = [
  "ok",
  "invalid_base",
  "start_missing",
  "stop_missing",
  "frame_shift",
  "nonsense",
];

export interface BackendAnalysis {
  id: string;
  originalSequence: string;
  resultType: string;
  positionStart: number | null;
  positionStop: number | null;
  readingFrame: string | null;
  codingRegion: string | null;
  preMrna: string | null;
  message: string | null;
  analysisDate: string;
}

export function mapBackendStatus(resultType: string): AnalysisStatus {
  switch (resultType) {
    case "CORRECT": return "ok";
    case "INVALID_BASE": return "invalid_base";
    case "START_CODON_NOT_FOUND": return "start_missing";
    case "STOP_CODON_NOT_FOUND": return "stop_missing";
    case "FRAME_SHIFT": return "frame_shift";
    case "NONSENSE_MUTATION": return "nonsense";
    default: return "invalid_base";
  }
}

export interface SequenceDetail {
  analysis: BackendAnalysis;
  sequence: string;
  status: AnalysisStatus;
  gcContent: number;
  invalidIndex: number | null;
  invalidChar: string | null;
  startIndex: number | null;
  stopIndex: number | null;
  stopIndexes: number[];
  frameStarts: number[];
  remainder: number;
  codingStart: number | null;
  codingEnd: number | null;
  preMrna: string | null;
}

export function normalizeSequence(raw: string): string {
  return raw
    .replace(/^\uFEFF/, "")
    .split("\n")
    .filter((line) => !line.trim().startsWith(">"))
    .join("")
    .replace(/\s/g, "")
    .toUpperCase();
}

export function buildDetail(analysis: BackendAnalysis): SequenceDetail {
  const sequence = normalizeSequence(analysis.originalSequence ?? "");
  const status = mapBackendStatus(analysis.resultType);
  const gcContent = sequence.length
    ? Math.round(((sequence.match(/[GC]/g)?.length ?? 0) / sequence.length) * 1000) / 10
    : 0;

  const invalid = sequence.match(/[^ACGT]/);
  const startIndex = analysis.positionStart ?? null;
  const stopIndex = analysis.positionStop ?? null;
  const frameStarts: number[] = [];

  if (startIndex !== null) {
    for (let i = startIndex; i + 3 <= sequence.length; i += 3) frameStarts.push(i);
  }

  const remainder = startIndex === null ? 0 : (sequence.length - startIndex) % 3;
  const stopIndexes: number[] = [];
  if (stopIndex !== null) stopIndexes.push(stopIndex);

  return {
    analysis,
    sequence,
    status,
    gcContent,
    invalidIndex: invalid?.index ?? null,
    invalidChar: invalid?.[0] ?? null,
    startIndex,
    stopIndex,
    stopIndexes,
    frameStarts,
    remainder,
    codingStart: analysis.codingRegion && startIndex !== null ? startIndex : null,
    codingEnd: analysis.codingRegion && stopIndex !== null ? stopIndex + 3 : null,
    preMrna: analysis.preMrna ?? null,
  };
}
