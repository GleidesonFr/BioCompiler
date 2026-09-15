export type AnalysisStatus =
  | "ok"
  | "invalid_base"
  | "start_missing"
  | "stop_missing"
  | "frame_shift"
  | "nonsense"
  | "five_prime_site"
  | "branch_point"
  | "three_prime_site"
  | "incomplete_intron"
  | "alternative_splicing";

export type SequenceType = "DNA" | "PRE_MRNA";

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
  five_prime_site: { label: "Sítio 5' ausente", short: "Sítio 5'", tone: "bad", severity: "erro" },
  branch_point: { label: "Branch point inválido", short: "Branch point", tone: "bad", severity: "erro" },
  three_prime_site: { label: "Sítio 3' ausente", short: "Sítio 3'", tone: "bad", severity: "erro" },
  incomplete_intron: { label: "Íntron incompleto", short: "Íntron incompleto", tone: "warn", severity: "alerta" },
  alternative_splicing: { label: "Splicing alternativo", short: "Splicing alternativo", tone: "warn", severity: "alerta" },
};

export const STATUS_ORDER: AnalysisStatus[] = [
  "ok",
  "invalid_base",
  "start_missing",
  "stop_missing",
  "frame_shift",
  "nonsense",
];

export const RNA_STATUS_ORDER: AnalysisStatus[] = [
  "ok",
  "invalid_base",
  "five_prime_site",
  "branch_point",
  "three_prime_site",
  "incomplete_intron",
  "alternative_splicing",
];

export interface BackendAnalysis {
  id: string;
  originalSequence: string;
  sequenceType?: SequenceType | null;
  resultType: string;
  positionStart: number | null;
  positionStop: number | null;
  readingFrame: string | null;
  codingRegion: string | null;
  preMrna: string | null;
  matureMrna?: string | null;
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
    case "FIVE_PRIME_SITE_ERROR": return "five_prime_site";
    case "BRANCH_POINT_ERROR": return "branch_point";
    case "THREE_PRIME_SITE_ERROR": return "three_prime_site";
    case "INCOMPLETE_INTRON": return "incomplete_intron";
    case "ALTERNATIVE_SPLICING": return "alternative_splicing";
    default: return "invalid_base";
  }
}

export interface SequenceDetail {
  analysis: BackendAnalysis;
  sequenceType: SequenceType;
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
  matureMrna: string | null;
}

export function getSequenceType(analysis: BackendAnalysis): SequenceType {
  if (analysis.sequenceType === "PRE_MRNA" || analysis.sequenceType === "DNA") {
    return analysis.sequenceType;
  }
  return analysis.originalSequence?.toUpperCase().includes("U") ? "PRE_MRNA" : "DNA";
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
  const sequenceType = getSequenceType(analysis);
  const status = mapBackendStatus(analysis.resultType);
  const gcContent = sequence.length
    ? Math.round(((sequence.match(/[GC]/g)?.length ?? 0) / sequence.length) * 1000) / 10
    : 0;

  const invalid = sequence.match(sequenceType === "DNA" ? /[^ACGT]/ : /[^ACGU]/);
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
    sequenceType,
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
    matureMrna: analysis.matureMrna ?? null,
  };
}
