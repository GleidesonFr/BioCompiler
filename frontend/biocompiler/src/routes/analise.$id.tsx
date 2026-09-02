import { createFileRoute, Link } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { ArrowDown, ArrowLeft } from "lucide-react";
import { Navbar } from "@/components/Navbar";
import { SeverityBadge, StatusPill } from "@/components/StatusBadge";
import { Button } from "@/components/ui/button";
import { buildDetail, STATUS_META, type SequenceDetail } from "@/lib/dna";
import { API } from "@/lib/api";
import { cn } from "@/lib/utils";

export const Route = createFileRoute("/analise/$id")({
  head: () => ({
    meta: [
      { title: "Detalhe da sequência — BioCompiler 1.0" },
      {
        name: "description",
        content:
          "Visualize quadro de leitura, start, stop, região codificadora e pré-mRNA da sequência analisada.",
      },
      { property: "og:title", content: "Detalhe da sequência — BioCompiler 1.0" },
      {
        property: "og:description",
        content: "Quadro de leitura, start, stop, região codificadora e pré-mRNA.",
      },
    ],
  }),
  component: AnalisePage,
});

function AnalisePage() {
  const { id } = Route.useParams();
  const [analysis, setAnalysis] = useState<Awaited<ReturnType<typeof API.getAnalysis>> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    setLoading(true);
    API.getAnalysis(id)
      .then((data) => active && setAnalysis(data))
      .catch((err) => active && setError(err instanceof Error ? err.message : "Análise não encontrada."))
      .finally(() => active && setLoading(false));
    return () => { active = false; };
  }, [id]);

  const detail = analysis ? buildDetail(analysis) : null;

  return (
    <div className="min-h-screen">
      <Navbar />
      <main className="mx-auto w-full max-w-5xl px-5 py-12">
        <Button variant="brandOutline" size="sm" asChild>
          <Link to="/historico"><ArrowLeft className="size-4" /> Voltar ao histórico</Link>
        </Button>

        {loading && <p className="mt-10 text-sm text-muted-foreground">Carregando análise...</p>}
        {!loading && error && <p className="mt-10 text-sm text-destructive">{error}</p>}
        {!loading && !error && !detail && <p className="mt-10 text-sm text-muted-foreground">Sequência não encontrada.</p>}

        {detail && (
          <>
            <header className="mt-6 grid grid-cols-[minmax(0,1fr)_auto] items-center gap-4">
              <div className="min-w-0">
                <h1 className="truncate text-3xl font-semibold">Análise #{detail.analysis.id}</h1>
                <p className="mt-2 text-sm text-muted-foreground">
                  {new Date(detail.analysis.analysisDate).toLocaleString("pt-BR")} · {detail.sequence.length} bases · GC {detail.gcContent}%
                </p>
              </div>
            </header>

            <section className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              <Info label="Quadro de leitura">{detail.analysis.readingFrame ?? "Indefinido"}</Info>
              <Info label="START (ATG)">{detail.startIndex === null ? "Não encontrado" : `posição ${detail.startIndex + 1}`}</Info>
              <Info label="STOP">{detail.stopIndex === null ? "Não encontrado" : `${detail.sequence.slice(detail.stopIndex, detail.stopIndex + 3)} na posição ${detail.stopIndex + 1}`}</Info>
              <Info label="Região codificadora">{detail.analysis.codingRegion ?? "Não definida"}</Info>
              <Info label="Resultado">{STATUS_META[detail.status].label}</Info>
              <Info label="Mensagem">{detail.analysis.message ?? "—"}</Info>
            </section>

            <SequenceViewer detail={detail} />
          </>
        )}
      </main>
    </div>
  );
}
function Info({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="rounded-2xl border border-border bg-card p-4 shadow-panel">
      <p className="text-xs text-muted-foreground">{label}</p>
      <p className="mt-1 font-display text-sm font-semibold">{children}</p>
    </div>
  );
}

function SequenceViewer({ detail }: { detail: SequenceDetail }) {
  const { status, sequence, frameStarts } = detail;
  const animated =
    status === "start_missing" ||
    status === "stop_missing" ||
    status === "frame_shift" ||
    status === "nonsense";

  const scanStarts =
    status === "start_missing"
      ? Array.from({ length: Math.max(0, Math.floor(sequence.length / 3)) }, (_, index) => index * 3)
      : status === "nonsense" && detail.startIndex !== null
        ? Array.from(
            { length: Math.max(0, Math.floor((sequence.length - detail.startIndex) / 3)) },
            (_, index) => detail.startIndex + index * 3,
          )
        : frameStarts;

  const [step, setStep] = useState(0);
  const [done, setDone] = useState(!animated);
  const [transcriptionStep, setTranscriptionStep] = useState(0);

  useEffect(() => {
    if (!animated) return;
    setStep(0);
    setDone(false);
    let i = 0;
    const timer = window.setInterval(() => {
      i += 1;
      setStep(i);
      if (i >= scanStarts.length) {
        window.clearInterval(timer);
        setDone(true);
      }
    }, 360);
    return () => window.clearInterval(timer);
  }, [animated, scanStarts.length, sequence]);

  useEffect(() => {
    if (status !== "ok" || !detail.preMrna) return;
    setTranscriptionStep(0);
    let i = 0;
    const timer = window.setInterval(() => {
      i += 1;
      setTranscriptionStep(i);
      if (i >= sequence.length) {
        window.clearInterval(timer);
      }
    }, 260);
    return () => window.clearInterval(timer);
  }, [status, sequence, detail.preMrna]);

  const currentTriple = animated && !done ? scanStarts[step] : undefined;
  const sweepCursor = currentTriple ?? (done ? sequence.length : -1);
  const nonsenseStops =
    status === "nonsense" && detail.startIndex !== null
      ? scanStarts.filter((start) => ["TAA", "TAG", "TGA"].includes(sequence.slice(start, start + 3)))
      : [];
  const tailStart = sequence.length - detail.remainder;

  const passedStopTriplet = (start: number) => {
    if (sweepCursor < 0) return false;
    return start + 3 <= sweepCursor;
  };

  const charClass = (i: number) => {
    if (status === "invalid_base") {
      return i === detail.invalidIndex
        ? "text-destructive blink-soft font-bold"
        : "text-muted-foreground";
    }
    if (status === "ok") {
      if (detail.startIndex !== null && i >= detail.startIndex && i < detail.startIndex + 3)
        return "text-ok font-bold bg-ok/15 rounded-sm";
      if (detail.stopIndex !== null && i >= detail.stopIndex && i < detail.stopIndex + 3)
        return "text-destructive font-bold bg-destructive/15 rounded-sm";
      return "text-foreground/70";
    }
    if (status === "nonsense") {
      const isStartCodon = detail.startIndex !== null && i >= detail.startIndex && i < detail.startIndex + 3;
      if (isStartCodon) {
        return "text-ok font-bold bg-ok/15 rounded-sm";
      }
      if (nonsenseStops.some((s) => i >= s && i < s + 3 && passedStopTriplet(s))) {
        return "text-destructive font-bold bg-destructive/15 rounded-sm blink-soft";
      }
      if (currentTriple !== undefined && i >= currentTriple && i < currentTriple + 3)
        return "text-secondary font-bold bg-secondary/20 rounded-sm";
      return "text-foreground/60";
    }
    if (
      currentTriple !== undefined &&
      i >= currentTriple &&
      i < currentTriple + 3
    )
      return "text-secondary font-bold bg-secondary/20 rounded-sm";
    if (done && status === "stop_missing") {
      const stopStarts = scanStarts.filter((start) => ["TAA", "TAG", "TGA"].includes(sequence.slice(start, start + 3)));
      if (stopStarts.some((start) => i >= start && i < start + 3)) {
        return "text-destructive blink-3";
      }
    }
    if (done && status === "start_missing")
      return "text-destructive blink-3";
    if (done && status === "frame_shift" && i >= tailStart)
      return "text-destructive font-bold bg-destructive/15 rounded-sm blink-soft";
    if (detail.startIndex !== null && i >= detail.startIndex && i < detail.startIndex + 3)
      return "text-ok font-bold bg-ok/15 rounded-sm";
    return "text-foreground/60";
  };

  return (
    <section className="mt-8 rounded-3xl border border-border bg-card p-6 shadow-panel">
      <h2 className="text-lg font-semibold">Sequência de DNA</h2>
      <p className="mt-1 text-xs text-muted-foreground">{legend(status)}</p>

      <div className="mt-4 flex flex-wrap gap-x-[3px] gap-y-2 font-mono text-sm sm:text-base">
        {sequence.split("").map((c, i) => (
          <span key={i} className={cn("px-[1px] transition-colors", charClass(i))}>
            {c}
          </span>
        ))}
      </div>

      {status === "ok" && detail.preMrna && (
        <div className="mt-8 animate-fade-in">
          <div className="flex flex-col items-center text-secondary">
            <ArrowDown className="size-6 animate-bounce" />
            <span className="mt-1 text-xs font-medium">Transcrição → pré-mRNA</span>
          </div>
          <div className="mt-4 flex flex-wrap gap-x-[3px] gap-y-2 font-mono text-sm text-foreground/70 sm:text-base">
            {detail.preMrna.split("").map((c, i) => {
              const swept = i < transcriptionStep;
              const display = swept ? c : detail.sequence[i] ?? c;
              const isTranscribed = swept && c === "U";

              return (
                <span
                  key={i}
                  className={cn(
                    "px-[1px] transition-colors",
                    isTranscribed && "mrna-flip text-secondary font-semibold",
                    !isTranscribed && swept && "text-secondary/80",
                  )}
                >
                  {display}
                </span>
              );
            })}
          </div>
        </div>
      )}
    </section>
  );
}

function legend(status: SequenceDetail["status"]) {
  switch (status) {
    case "ok":
      return "Start em verde, stop em vermelho e o pré-mRNA transcrito abaixo.";
    case "invalid_base":
      return "A base destacada em vermelho não pertence ao alfabeto A, T, C, G.";
    case "start_missing":
      return "Varredura em trincas procurando ATG — nenhum start encontrado.";
    case "stop_missing":
      return "Varredura em trincas a partir do start procurando TAA, TAG ou TGA.";
    case "frame_shift":
      return "A leitura em trincas não fecha: sobram bases no final da sequência.";
    case "nonsense":
      return "Mais de um STOP no mesmo quadro de leitura do start.";
  }
}
