import { STATUS_META, type AnalysisStatus } from "@/lib/dna";
import { cn } from "@/lib/utils";

const TONES: Record<string, string> = {
  ok: "bg-ok/15 text-ok",
  warn: "bg-warn/20 text-warn",
  bad: "bg-destructive/15 text-destructive",
};

export function StatusPill({ status }: { status: AnalysisStatus }) {
  const meta = STATUS_META[status];
  return (
    <span
      className={cn(
        "inline-flex items-center rounded-full px-3 py-1 text-xs font-semibold",
        TONES[meta.tone],
      )}
    >
      {meta.short}
    </span>
  );
}

export function SeverityBadge({ status }: { status: AnalysisStatus }) {
  const meta = STATUS_META[status];
  const label =
    meta.severity === "aprovado"
      ? "Aprovado"
      : meta.severity === "alerta"
        ? "Alerta"
        : "Erro";
  return (
    <span
      className={cn(
        "inline-flex items-center rounded-full px-3 py-1 text-xs font-semibold capitalize",
        TONES[meta.tone],
      )}
    >
      {label}
    </span>
  );
}
