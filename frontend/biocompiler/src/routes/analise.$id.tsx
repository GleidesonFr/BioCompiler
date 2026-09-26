import { createFileRoute, Link } from "@tanstack/react-router";
import { useEffect, useMemo, useState } from "react";
import { ArrowDown, ArrowLeft, FlaskConical, Scissors } from "lucide-react";
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
                  {new Date(detail.analysis.analysisDate).toLocaleString("pt-BR")} · {detail.sequence.length} bases · {detail.sequenceType === "DNA" ? `GC ${detail.gcContent}%` : detail.sequenceType === "MATURE_MRNA" ? "mRNA maduro" : "pré-mRNA"}
                </p>
              </div>
            </header>

            {detail.sequenceType === "DNA" ? (
              <section className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                <Info label="Quadro de leitura">{detail.analysis.readingFrame ?? "Indefinido"}</Info>
                <Info label="START (ATG)">{detail.startIndex === null ? "Não encontrado" : `posição ${detail.startIndex + 1}`}</Info>
                <Info label="STOP">{detail.stopIndex === null ? "Não encontrado" : `${detail.sequence.slice(detail.stopIndex, detail.stopIndex + 3)} na posição ${detail.stopIndex + 1}`}</Info>
                <Info label="Região codificadora">{detail.analysis.codingRegion ?? "Não definida"}</Info>
                <Info label="Resultado">{STATUS_META[detail.status].label}</Info>
                <Info label="Mensagem">{detail.analysis.message ?? "—"}</Info>
              </section>
            ) : detail.sequenceType === "MATURE_MRNA" ? (
              <MatureMrnaInfo detail={detail} />
            ) : (
              <section className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                <Info label="Processamento">Splicing e maturação</Info>
                <Info label="CAP 5'">{detail.status === "ok" ? "m7Gppp adicionada" : "Não gerada"}</Info>
                <Info label="Cauda poli-A">{detail.status === "ok" ? "100 adeninas" : "Não gerada"}</Info>
                <Info label="Resultado">{STATUS_META[detail.status].label}</Info>
                <Info label="Mensagem">{detail.analysis.message ?? "—"}</Info>
              </section>
            )}

            {detail.sequenceType === "DNA" ? <SequenceViewer detail={detail} /> : detail.sequenceType === "MATURE_MRNA" ? <RibosomeViewer detail={detail} /> : <RnaSequenceViewer detail={detail} />}
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

const MRNA_CAP = "m7Gppp";
const STOP_CODONS = ["UAA", "UAG", "UGA"];

function splitMatureMrna(sequence: string) {
  const hasCap = sequence.startsWith(MRNA_CAP);
  const polyAMatch = sequence.match(/A+$/i);
  const polyALength = polyAMatch?.[0].length ?? 0;
  const bodyStart = hasCap ? MRNA_CAP.length : 0;
  const bodyEnd = Math.max(bodyStart, sequence.length - polyALength);

  return {
    hasCap,
    polyALength,
    rnaBody: sequence.slice(bodyStart, bodyEnd).toUpperCase(),
  };
}

function MatureMrnaInfo({ detail }: { detail: SequenceDetail }) {
  const { hasCap, polyALength } = splitMatureMrna(detail.sequence);
  const transcriptionFailed = detail.status === "cap_5_error";

  return (
    <section className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      <Info label="CAP 5'">{hasCap ? "m7Gppp — presente" : "BUG: ausente ou inválida"}</Info>
      <Info label="Cauda poli-A">
        <span className={detail.status === "poly_a_error" ? "text-destructive" : undefined}>
          {polyALength} adeninas
        </span>
      </Info>
      <Info label="START (AUG)">
        {transcriptionFailed ? "Falha de transcrição" : detail.startIndex === null ? "Não encontrado" : `posição ${detail.startIndex + 1}`}
      </Info>
      <Info label="STOP">
        {transcriptionFailed ? "Falha de transcrição" : detail.stopIndex === null ? "Não encontrado" : `posição ${detail.stopIndex + 1}`}
      </Info>
      <Info label="Resultado">{STATUS_META[detail.status].label}</Info>
      <Info label="Mensagem">{detail.analysis.message ?? "—"}</Info>
    </section>
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
    default:
      return "Visualização disponível para análises de DNA.";
  }
}

function RnaSequenceViewer({ detail }: { detail: SequenceDetail }) {
  const { sequence, status, matureMrna } = detail;
  const canonicalFivePrimeStart = sequence.length >= 14 ? 12 : -1;
  const intronStart = sequence.startsWith("GU", 12) ? 12 : sequence.indexOf("GU");
  const intronEnd = intronStart >= 0 ? sequence.indexOf("AG", intronStart + 2) : -1;
  const branchStart = intronEnd >= 0 ? Math.max(intronStart + 2, intronEnd - 30) : -1;
  const branchEnd = intronEnd >= 0 ? intronEnd - 10 : -1;
  const branchPoint = branchStart >= 0
    ? Array.from({ length: Math.max(0, branchEnd - branchStart + 1) }, (_, offset) => branchStart + offset)
      .find((index) => sequence[index] === "A") ?? -1
    : -1;
  const [step, setStep] = useState(0);
  const [done, setDone] = useState(false);
  const [scanPair, setScanPair] = useState(-1);
  const [scanDone, setScanDone] = useState(false);
  const isError = status !== "ok";
  const fivePrimeStart = intronStart >= 0 ? intronStart : canonicalFivePrimeStart;
  const threePrimeStart = intronEnd >= 0 ? intronEnd : sequence.length >= 36 ? 34 : -1;
  const alternativeEnds = intronStart >= 0
    ? Array.from({ length: sequence.length - intronStart }, (_, offset) => intronStart + offset)
      .filter((index) => sequence.slice(index, index + 2) === "AG")
    : [];

  useEffect(() => {
    setStep(0);
    setDone(false);
    const timers = [
      window.setTimeout(() => setStep(1), 350),
      window.setTimeout(() => setStep(2), 760),
      window.setTimeout(() => setStep(3), 1170),
      window.setTimeout(() => {
        setStep(4);
        setDone(true);
      }, 1580),
    ];
    return () => timers.forEach(window.clearTimeout);
  }, [sequence, status]);

  useEffect(() => {
    const scansFivePrimeSite = status === "five_prime_site";
    const scansThreePrimeSite = status === "three_prime_site" && intronStart >= 0;
    if (!scansFivePrimeSite && !scansThreePrimeSite) {
      setScanPair(-1);
      setScanDone(false);
      return;
    }

    const firstPair = scansFivePrimeSite ? 0 : intronStart + 2;
    setScanPair(firstPair);
    setScanDone(false);

    const timer = window.setInterval(() => {
      setScanPair((currentPair) => {
        const nextPair = currentPair + 2;
        if (nextPair >= sequence.length) {
          window.clearInterval(timer);
          setScanDone(true);
          return currentPair;
        }
        return nextPair;
      });
    }, 180);

    return () => window.clearInterval(timer);
  }, [intronStart, sequence.length, status]);

  const baseClass = (index: number) => {
    if (status === "invalid_base") {
      return index === detail.invalidIndex ? "bg-destructive/15 text-destructive font-bold blink-soft" : "text-muted-foreground";
    }
    if (status === "five_prime_site") {
      if (scanDone) return "text-destructive font-bold rna-error-glow";
      if (index >= scanPair && index < scanPair + 2) return "bg-secondary/20 text-secondary font-bold";
      return "text-foreground/60";
    }
    if (status === "three_prime_site") {
      if (scanDone && intronStart >= 0 && index >= intronStart) return "text-destructive font-bold rna-error-glow";
      if (index >= scanPair && index < scanPair + 2) return "bg-secondary/20 text-secondary font-bold";
      return "text-foreground/60";
    }
    if (status === "branch_point" && intronStart >= 0 && intronEnd >= 0) {
      if (index >= intronStart && index < intronStart + 2 || index >= intronEnd && index < intronEnd + 2) {
        return "bg-secondary/20 text-secondary font-bold";
      }
      if (index > intronStart + 1 && index < intronEnd) {
        return "bg-destructive/15 text-destructive font-bold rna-error-glow";
      }
    }
    if (fivePrimeStart >= 0 && index >= fivePrimeStart && index < fivePrimeStart + 2) {
      return step >= 1 ? "bg-secondary/20 text-secondary font-bold" : "text-foreground/70";
    }
    if (index === branchPoint) {
      return step >= 2 ? "bg-warn/20 text-warn font-bold" : "text-foreground/70";
    }
    if (threePrimeStart >= 0 && index >= threePrimeStart && index < threePrimeStart + 2) {
      return step >= 3 ? "bg-secondary/20 text-secondary font-bold" : "text-foreground/70";
    }
    if (status === "incomplete_intron" && fivePrimeStart >= 0 && index >= fivePrimeStart && step >= 2) return "text-warn blink-soft";
    if (status === "alternative_splicing" && alternativeEnds.some((end) => index >= end && index < end + 2) && step >= 3) {
      return "bg-warn/20 text-warn font-bold blink-soft";
    }
    return "text-foreground/70";
  };

  const animationMessage = () => {
    if (status === "ok") return "Reconhecendo sítios, removendo o íntron e revelando o mRNA maduro.";
    if (status === "five_prime_site") return "Procurando o sítio 5' GU na posição esperada.";
    if (status === "branch_point") return "O íntron foi delimitado, mas nenhum branch point A atende à distância exigida.";
    if (status === "three_prime_site") return "O sítio 5' foi encontrado; a busca pelo sítio 3' AG não foi concluída.";
    if (status === "incomplete_intron") return "A leitura iniciou no sítio 5', mas a estrutura terminou antes de completar o íntron.";
    if (status === "alternative_splicing") return "Mais de um sítio 3' válido foi reconhecido; o processamento permanece ambíguo.";
    return "A sequência foi validada contra o alfabeto de RNA A, C, G e U.";
  };

  return (
    <section className="mt-8 rounded-3xl border border-border bg-card p-6 shadow-panel">
      <h2 className="text-lg font-semibold">Pré-mRNA</h2>
      <p className="mt-1 text-xs text-muted-foreground">{animationMessage()}</p>

      <div className="mt-4 flex flex-wrap gap-x-[3px] gap-y-2 font-mono text-sm sm:text-base">
        {sequence.split("").map((base, index) => (
          <span key={index} className={cn("rounded-sm px-[1px] transition-colors", baseClass(index))}>{base}</span>
        ))}
      </div>

      {status === "ok" && matureMrna ? (
        <div className={cn("mt-8", step >= 4 ? "animate-fade-in" : "opacity-40")}>
          <div className="flex items-center justify-center gap-2 text-secondary">
            <Scissors className={cn("size-5", step === 3 && "animate-pulse")} />
            <span className="text-xs font-medium">Splicing: íntron removido, éxons unidos</span>
          </div>
          <div className="mt-4 rounded-2xl border border-secondary/20 bg-secondary/5 p-4">
            <p className="text-xs font-medium text-secondary">mRNA maduro</p>
            <p className="mt-2 max-h-36 overflow-y-auto break-all font-mono text-sm leading-6 text-foreground/80">{matureMrna}</p>
            <div className="mt-3 flex flex-wrap gap-2 text-xs">
              <span className="rounded-full bg-secondary/15 px-2.5 py-1 font-medium text-secondary">CAP 5': m7Gppp</span>
              <span className="rounded-full bg-ok/15 px-2.5 py-1 font-medium text-ok">Cauda poli-A: 100 A</span>
            </div>
          </div>
        </div>
      ) : isError && (
        <div className={cn("mt-8 flex flex-col items-center text-center", done ? "animate-fade-in" : "opacity-40")}>
          <ArrowDown className="size-5 text-destructive" />
          <p className="mt-2 text-sm font-medium text-destructive">{STATUS_META[status].label}</p>
          <p className="mt-1 text-xs text-muted-foreground">mRNA maduro não gerado.</p>
        </div>
      )}
    </section>
  );
}

function RibosomeViewer({ detail }: { detail: SequenceDetail }) {
  const { status, analysis } = detail;
  const { hasCap, polyALength, rnaBody } = splitMatureMrna(detail.sequence);

  let incompleteFragmentStart: number | null = null;
  if (status === "reading_frame_error" && detail.startIndex !== null) {
    const lastStopIndex = Math.max(
      ...STOP_CODONS.map((stopCodon) => rnaBody.lastIndexOf(stopCodon)),
    );
    const candidateStart = lastStopIndex + 3;
    const fragmentLength = rnaBody.length - candidateStart;
    if (lastStopIndex > detail.startIndex && fragmentLength > 0 && fragmentLength < 3) {
      incompleteFragmentStart = candidateStart;
    }
  }

  const codons: string[] = [];
  if (analysis.codingRegion) {
    const cr = analysis.codingRegion.toUpperCase();
    for (let i = 0; i + 3 <= cr.length; i += 3) codons.push(cr.slice(i, i + 3));
  }

  const aminoAcids = analysis.protein ? analysis.protein.split("-") : [];
  const [revealedCodons, setRevealedCodons] = useState(0);
  const [scanStep, setScanStep] = useState(0);
  const [done, setDone] = useState(false);
  const [reduceMotion, setReduceMotion] = useState(false);
  const isCorrect = status === "ok";
  const isError = status !== "ok";
  const isScanningForStart = status === "start_missing";
  const isScanningForStop = status === "stop_missing" && detail.startIndex !== null;
  const scanStarts = useMemo(() => {
    const starts: number[] = [];
    if (isScanningForStart) {
      for (let i = 0; i + 3 <= rnaBody.length; i += 3) starts.push(i);
    } else if (isScanningForStop && detail.startIndex !== null) {
      for (let i = detail.startIndex; i + 3 <= rnaBody.length; i += 3) starts.push(i);
    }
    return starts;
  }, [detail.startIndex, isScanningForStart, isScanningForStop, rnaBody]);

  useEffect(() => {
    const mediaQuery = window.matchMedia("(prefers-reduced-motion: reduce)");
    const updatePreference = () => setReduceMotion(mediaQuery.matches);
    updatePreference();
    mediaQuery.addEventListener("change", updatePreference);
    return () => mediaQuery.removeEventListener("change", updatePreference);
  }, []);

  useEffect(() => {
    setRevealedCodons(0);
    setScanStep(0);
    setDone(false);

    if (reduceMotion) {
      setRevealedCodons(codons.length);
      setDone(true);
      return;
    }

    if (isScanningForStart || isScanningForStop) {
      if (scanStarts.length === 0) {
        const timeout = window.setTimeout(() => setDone(true), 800);
        return () => window.clearTimeout(timeout);
      }

      let step = 0;
      const timer = window.setInterval(() => {
        step += 1;
        if (step >= scanStarts.length) {
          window.clearInterval(timer);
          setDone(true);
          return;
        }
        setScanStep(step);
      }, 360);
      return () => window.clearInterval(timer);
    }

    if (!isCorrect || codons.length === 0) {
      const t = window.setTimeout(() => setDone(true), 800);
      return () => window.clearTimeout(t);
    }
    let i = 0;
    const timer = window.setInterval(() => {
      i += 1;
      setRevealedCodons(i);
      if (i >= codons.length) {
        window.clearInterval(timer);
        setDone(true);
      }
    }, 320);
    return () => window.clearInterval(timer);
  }, [isCorrect, isScanningForStart, isScanningForStop, reduceMotion, analysis.originalSequence, codons.length, scanStarts]);

  const currentScanStart = !done && (isScanningForStart || isScanningForStop)
    ? scanStarts[scanStep]
    : undefined;

  const ribosomeMessage = () => {
    if (status === "ok") return "O ribossomo leu os códons em trincas do AUG ao STOP e sintetizou a proteína.";
    if (status === "cap_5_error") return "BUG: a sequência não possui a CAP 5' m7Gppp correta. O ribossomo não consegue iniciar.";
    if (status === "poly_a_error") return "BUG: a cauda poli-A está incorreta (exatamente 100 adeninas são esperadas na extremidade 3').";
    if (status === "invalid_base") return "BUG: base inválida detectada na região de RNA (esperado A, U, G ou C).";
    if (status === "start_missing") return "BUG: códon de início AUG não encontrado na sequência de mRNA maduro.";
    if (status === "stop_missing") return "BUG: códon de parada (UAA, UAG ou UGA) não encontrado na mesma moldura do AUG.";
    if (status === "reading_frame_error") return "BUG: o quadro de leitura está deslocado — restam bases que não formam uma trinca completa.";
    return "Visualização da tradução ribossomal.";
  };

  return (
    <section className="mt-8 rounded-3xl border border-border bg-card p-6 shadow-panel">
      <h2 className="text-lg font-semibold">mRNA Maduro — Tradução Ribossomal</h2>
      <p className="mt-1 text-xs text-muted-foreground">{ribosomeMessage()}</p>

      {/* CAP 5' + RNA body + poly-A */}
      <div className="mt-5 flex flex-wrap items-center gap-x-[3px] gap-y-2 font-mono text-sm sm:text-base">
        <span className={cn("rounded-sm px-1 font-semibold", hasCap ? "bg-secondary/20 text-secondary" : "bg-destructive/15 text-destructive blink-soft")}>
          {hasCap ? "m7Gppp" : "[sem CAP 5']"}
        </span>

        {rnaBody.split("").map((base, i) => {
          const startIdx = detail.startIndex;
          const stopIdx = detail.stopIndex;
          const codonOffset = startIdx !== null ? i - startIdx : -1;
          const codonIndex = codonOffset >= 0 ? Math.floor(codonOffset / 3) : -1;
          const isInCodingRegion = startIdx !== null && stopIdx !== null && i >= startIdx && i < stopIdx + 3;
          const isStart = startIdx !== null && i >= startIdx && i < startIdx + 3;
          const isStop = stopIdx !== null && i >= stopIdx && i < stopIdx + 3;
          const isRevealed = codonIndex >= 0 && codonIndex < revealedCodons;
          const isIncompleteFrameFragment = incompleteFragmentStart !== null && i >= incompleteFragmentStart;
          const isCurrentScanTriplet = currentScanStart !== undefined && i >= currentScanStart && i < currentScanStart + 3;

          let cls = "text-foreground/50";
          if (isCurrentScanTriplet) cls = "text-secondary font-bold bg-secondary/20 rounded-sm";
          else if (isStop) cls = "text-destructive font-bold bg-destructive/15 rounded-sm";
          else if (isStart) cls = "text-ok font-bold bg-ok/15 rounded-sm";
          else if (isInCodingRegion && isRevealed) cls = "text-secondary font-semibold bg-secondary/10 rounded-sm";
          if (isIncompleteFrameFragment) cls = "text-destructive font-bold bg-destructive/15 rounded-sm blink-soft";

          return (
            <span key={i} className={cn("px-[1px] transition-colors duration-200", cls)}>{base}</span>
          );
        })}

        <span
          className={cn(
            "ml-1 rounded-sm px-1 text-xs font-semibold",
            status === "poly_a_error"
              ? "bg-destructive/15 text-destructive"
              : "bg-ok/10 text-ok",
          )}
        >
          poli-A({polyALength})
        </span>
      </div>

      {/* Codon / amino-acid table */}
      {isCorrect && codons.length > 0 && (
        <div className={cn("mt-8", done ? "animate-fade-in" : "opacity-40")}>
          <div className="flex items-center gap-2 text-secondary">
            <FlaskConical className="size-5" />
            <span className="text-xs font-medium">Tradução: códons → aminoácidos</span>
          </div>
          <div className="mt-3 overflow-x-auto">
            <div className="flex gap-1 min-w-max pb-2">
              {codons.map((codon, idx) => {
                const aa = aminoAcids[idx] ?? "???";
                const isStopCodon = ["UAA", "UAG", "UGA"].includes(codon);
                const revealed = idx < revealedCodons;
                return (
                  <div
                    key={idx}
                    className={cn(
                      "flex flex-col items-center rounded-lg border px-2 py-1.5 text-center transition-all duration-300",
                      revealed
                        ? isStopCodon
                          ? "border-destructive/30 bg-destructive/10"
                          : idx === 0
                          ? "border-ok/30 bg-ok/10"
                          : "border-secondary/20 bg-secondary/5"
                        : "border-border bg-muted/30 opacity-30",
                    )}
                  >
                    <span className={cn("font-mono text-xs font-bold", isStopCodon ? "text-destructive" : idx === 0 ? "text-ok" : "text-secondary")}>
                      {codon}
                    </span>
                    <span className="mt-0.5 text-[10px] text-muted-foreground">{aa}</span>
                  </div>
                );
              })}
            </div>
          </div>

          {done && analysis.protein && (
            <div className="mt-4 rounded-2xl border border-ok/20 bg-ok/5 p-4 animate-fade-in">
              <p className="text-xs font-medium text-ok">Proteína sintetizada</p>
              <p className="mt-2 max-h-36 overflow-y-auto break-all font-mono text-sm leading-6 text-foreground/80">
                {analysis.protein}
              </p>
              <div className="mt-3 flex flex-wrap gap-1.5 text-xs">
                <span className="rounded-full bg-secondary/15 px-2.5 py-1 font-medium text-secondary">
                  {aminoAcids.length} aminoácido(s)
                </span>
                <span className="rounded-full bg-ok/15 px-2.5 py-1 font-medium text-ok">
                  {codons.length} códon(s) lido(s)
                </span>
              </div>
            </div>
          )}
        </div>
      )}

      {isError && done && (
        <div className="mt-8 flex flex-col items-center text-center animate-fade-in">
          <ArrowDown className="size-5 text-destructive" />
          <p className="mt-2 text-sm font-medium text-destructive">{STATUS_META[status].label}</p>
          <p className="mt-1 text-xs text-muted-foreground">Proteína não sintetizada.</p>
        </div>
      )}
    </section>
  );
}
