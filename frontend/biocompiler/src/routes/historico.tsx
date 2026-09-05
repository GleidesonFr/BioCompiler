import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { useEffect, useMemo, useState } from "react";
import { Download, LoaderCircle, Trash2 } from "lucide-react";
import { Navbar } from "@/components/Navbar";
import { SeverityBadge } from "@/components/StatusBadge";
import { Button } from "@/components/ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { mapBackendStatus, STATUS_META, STATUS_ORDER, type AnalysisStatus } from "@/lib/dna";
import { API } from "@/lib/api";
import { useHistory } from "@/lib/history";
import { cn } from "@/lib/utils";
import { DnaLoader } from "@/components/DnaLoader";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { toast } from "sonner";

const PAGE_SIZE = 8;

export const Route = createFileRoute("/historico")({
  head: () => ({
    meta: [
      { title: "Histórico de análises — BioCompiler 1.0" },
      { name: "description", content: "Veja todas as sequências analisadas e seus resultados." },
    ],
  }),
  component: Historico,
});

function StatNumber({ value, tone }: { value: number; tone: string }) {
  const [displayValue, setDisplayValue] = useState(0);

  useEffect(() => {
    const start = displayValue;
    const end = value;
    if (start === end) return;

    let rafId = 0;
    const duration = 700;
    const startTime = performance.now();

    const tick = (now: number) => {
      const elapsed = now - startTime;
      const progress = Math.min(elapsed / duration, 1);
      const eased = 1 - Math.pow(1 - progress, 4);
      const nextValue = Math.round(start + (end - start) * eased);
      setDisplayValue(nextValue);

      if (progress < 1) {
        rafId = window.requestAnimationFrame(tick);
      }
    };

    rafId = window.requestAnimationFrame(tick);
    return () => window.cancelAnimationFrame(rafId);
  }, [value]);

  return (
    <p className={cn("mt-2 font-display text-3xl font-semibold stat-reveal", tone)}>
      {displayValue}
    </p>
  );
}

function Historico() {
  const [page, setPage] = useState(1);
  const [stats, setStats] = useState({
    correct: 0,
    invalidBase: 0,
    startMissing: 0,
    stopMissing: 0,
    frameshift: 0,
    nonsense: 0,
  });
  const [downloading, setDownloading] = useState(false);
  const [clearDialogOpen, setClearDialogOpen] = useState(false);
  const [clearing, setClearing] = useState(false);
  const { records, loading, error, refresh } = useHistory(page - 1, PAGE_SIZE);
  const navigate = useNavigate();

  useEffect(() => {
    let active = true;

    const loadStats = async () => {
      try {
        const nextStats = await API.getHistoryStats();
        if (active) setStats(nextStats);
      } catch {
        if (active) {
          setStats({
            correct: 0,
            invalidBase: 0,
            startMissing: 0,
            stopMissing: 0,
            frameshift: 0,
            nonsense: 0,
          });
        }
      }
    };

    void loadStats();
    return () => {
      active = false;
    };
  }, []);

  const counts = useMemo(() => {
    const base = Object.fromEntries(STATUS_ORDER.map((s) => [s, 0])) as Record<AnalysisStatus, number>;
    base.ok = stats.correct;
    base.invalid_base = stats.invalidBase;
    base.start_missing = stats.startMissing;
    base.stop_missing = stats.stopMissing;
    base.frame_shift = stats.frameshift;
    base.nonsense = stats.nonsense;
    return base;
  }, [stats]);

  useEffect(() => {
    if (records.totalPages > 0 && page > records.totalPages) {
      setPage(records.totalPages);
    }
    if (records.totalPages === 0 && page !== 1) {
      setPage(1);
    }
  }, [page, records.totalPages]);

  const totalPages = Math.max(1, records.totalPages || 1);
  const current = Math.min(page, totalPages);
  const rows = records.content;

  const clear = async () => {
    setClearing(true);
    try {
      await API.clearHistory();
      setPage(1);
      setStats({
        correct: 0,
        invalidBase: 0,
        startMissing: 0,
        stopMissing: 0,
        frameshift: 0,
        nonsense: 0,
      });
      await refresh(0, PAGE_SIZE);
      const nextStats = await API.getHistoryStats();
      setStats(nextStats);
      setClearDialogOpen(false);
      toast.success("Histórico limpo com sucesso.");
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "Não foi possível limpar o histórico.");
    } finally {
      setClearing(false);
    }
  };

  const download = async () => {
    setDownloading(true);
    try {
      await API.downloadHistory();
    } catch (err) {
      window.alert(err instanceof Error ? err.message : "Não foi possível baixar os dados.");
    } finally {
      setDownloading(false);
    }
  };

  return (
  <div className="min-h-screen">
    <Navbar />

    <main className="mx-auto w-full max-w-6xl px-5 py-12">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-3xl font-semibold">
            Histórico de análises
          </h1>

          <p className="mt-2 text-sm text-muted-foreground">
            {`${records.totalElements} análise(s) registrada(s) no servidor.`}
          </p>
        </div>

        <div className="flex flex-wrap gap-2">
          <Button
            onClick={() => void download()}
            disabled={downloading || records.totalElements === 0}
            title={records.totalElements === 0 ? "Não há dados para baixar" : "Baixar todos os dados da sessão"}
            className="bg-ok text-white shadow-[0_8px_24px_-10px_var(--ok)] hover:bg-ok/90"
          >
            {downloading ? <LoaderCircle className="size-4 animate-spin" /> : <Download className="size-4" />}
            {downloading ? "Preparando..." : "Baixar dados"}
          </Button>
          {records.totalElements > 0 && (
            <AlertDialog open={clearDialogOpen} onOpenChange={setClearDialogOpen}>
              <Button variant="brandOutline" onClick={() => setClearDialogOpen(true)}>
                <Trash2 className="size-4" />
                Limpar histórico
              </Button>
              <AlertDialogContent>
                <AlertDialogHeader>
                  <AlertDialogTitle>Limpar todo o histórico?</AlertDialogTitle>
                  <AlertDialogDescription>
                    Esta ação remove permanentemente todas as análises desta sessão e não pode ser desfeita.
                  </AlertDialogDescription>
                </AlertDialogHeader>
                <AlertDialogFooter>
                  <AlertDialogCancel disabled={clearing}>Cancelar</AlertDialogCancel>
                  <AlertDialogAction
                    className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                    disabled={clearing}
                    onClick={(event) => {
                      event.preventDefault();
                      void clear();
                    }}
                  >
                    {clearing ? "Limpando..." : "Limpar histórico"}
                  </AlertDialogAction>
                </AlertDialogFooter>
              </AlertDialogContent>
            </AlertDialog>
          )}
        </div>
      </div>

      <div className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {STATUS_ORDER.map((status) => {
          const meta = STATUS_META[status];
          const tone = meta.tone === "ok" ? "text-ok" : meta.tone === "warn" ? "text-warn" : "text-destructive";
          return (
            <div key={status} className="rounded-2xl border border-border bg-card p-5 shadow-panel">
              <p className="text-sm text-muted-foreground">{meta.label}</p>
              <StatNumber value={counts[status]} tone={tone} />
            </div>
          );
        })}
      </div>

      <div className="mt-10 overflow-hidden rounded-2xl border border-border bg-card shadow-panel">
        {loading ? (
          <div className="flex min-h-[220px] items-center justify-center gap-3 py-10">
            <DnaLoader />
          </div>
        ) : (
          <>
            <Table>
              <TableHeader><TableRow className="bg-muted/60"><TableHead className="w-16">#</TableHead><TableHead>Status</TableHead><TableHead>Detalhes</TableHead></TableRow></TableHeader>
              <TableBody>
                {error && <TableRow><TableCell colSpan={3} className="py-8 text-center text-sm text-destructive">{error}</TableCell></TableRow>}
                {rows.length === 0 && !loading && !error && <TableRow><TableCell colSpan={3} className="py-12 text-center text-sm text-muted-foreground">Nenhuma análise registrada ainda.</TableCell></TableRow>}
                {rows.map((record, i) => {
                  const status = mapBackendStatus(record.resultType);
                  return (
                    <TableRow key={record.id} tabIndex={0} role="link"
                      onClick={() => navigate({ to: "/analise/$id", params: { id: record.id } })}
                      onKeyDown={(e) => { if (e.key === "Enter") void navigate({ to: "/analise/$id", params: { id: record.id } }); }}
                      className="cursor-pointer transition-colors hover:bg-secondary/10">
                      <TableCell className="font-mono text-xs">{records.totalElements - ((current - 1) * PAGE_SIZE + i)}</TableCell>
                      <TableCell><SeverityBadge status={status} /></TableCell>
                      <TableCell className="text-sm text-muted-foreground">{record.message ?? STATUS_META[status].label}</TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          </>
        )}
      </div>

      <div className="mt-5 flex items-center justify-between gap-4">
        <p className="text-xs text-muted-foreground">Página {current} de {totalPages}</p>
        <div className="flex gap-2">
          <Button variant="brandOutline" size="sm" disabled={current <= 1} onClick={() => setPage(current - 1)}>Anterior</Button>
          <Button variant="brandOutline" size="sm" disabled={current >= totalPages} onClick={() => setPage(current + 1)}>Próxima</Button>
        </div>
      </div>
    </main>
  </div>
);
}
