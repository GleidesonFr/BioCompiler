import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { useRef, useState } from "react";
import { FileUp, Play, Sparkles, Type as TypeIcon } from "lucide-react";
import { Navbar } from "@/components/Navbar";
import { DnaLoaderOverlay } from "@/components/DnaLoader";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Input } from "@/components/ui/input";
import { API } from "@/lib/api";
import { cn } from "@/lib/utils";

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "BioCompiler 1.0 — Análise de sequências de DNA" },
      {
        name: "description",
        content:
          "Compile e valide sequências de DNA por texto ou arquivo: start codon, stop codon, frame shift e nonsense mutation.",
      },
      { property: "og:title", content: "BioCompiler 1.0 — Análise de sequências de DNA" },
      {
        property: "og:description",
        content: "Valide sequências de DNA por texto ou arquivo em segundos.",
      },
    ],
  }),
  component: Index,
});

function Index() {
  const [fileMode, setFileMode] = useState(false);
  const [text, setText] = useState("");
  const [file, setFile] = useState<File | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);
  const navigate = useNavigate();

  const handleAnalyze = async () => {
    setError(null);

    if (fileMode) {
      if (!file) {
        setError("Selecione um arquivo CSV ou TXT para analisar.");
        return;
      }

      setLoading(true);
      try {
        await API.analyzeFile(file);
        await navigate({ to: "/historico" });
      } catch (err) {
        setError(err instanceof Error ? err.message : "Erro ao processar o arquivo.");
      } finally {
        setLoading(false);
      }
      return;
    }

    const sequence = text.trim();
    if (!sequence) {
      setError("Digite uma sequência de DNA para analisar.");
      return;
    }

    setLoading(true);
    try {
      const analysis = await API.analyzeSequence(sequence);
      if (!analysis.id) {
        throw new Error("O backend retornou uma análise sem identificador.");
      }
      await navigate({ to: "/analise/$id", params: { id: analysis.id } });
    } catch (err) {
      setError(err instanceof Error ? err.message : "Erro ao analisar a sequência.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen">
      <Navbar />
      <main className="mx-auto w-full max-w-3xl px-5 py-14">
        <div className="mb-10 text-center">
          <span className="inline-flex items-center gap-2 rounded-full border border-secondary/30 bg-secondary/10 px-3 py-1 text-xs font-medium text-secondary">
            <Sparkles className="size-3.5" /> Compilador genético
          </span>
          <h1 className="mt-5 text-4xl font-semibold sm:text-5xl">
            Analise sua sequência de DNA
          </h1>
          <p className="mx-auto mt-3 max-w-lg text-sm text-muted-foreground">
            Cole a sequência ou envie um arquivo. O BioCompiler verifica bases, quadro de
            leitura e códons de início e parada.
          </p>
        </div>

        <section className="rounded-3xl border border-border bg-card p-6 shadow-panel sm:p-8">
          <div className="relative mb-6 grid grid-cols-2 rounded-2xl bg-muted/60 p-1.5">
            <span
              aria-hidden
              className={cn(
                "absolute inset-y-1.5 left-1.5 w-[calc(50%-6px)] rounded-xl bg-secondary shadow-glow transition-transform duration-300 ease-[cubic-bezier(0.22,1,0.36,1)]",
                fileMode && "translate-x-full",
              )}
            />
            <button
              type="button"
              onClick={() => {
                setFileMode(false);
                setError(null);
              }}
              className={cn(
                "relative z-10 flex items-center justify-center gap-2 rounded-xl py-2.5 text-sm font-medium transition-colors duration-300",
                fileMode ? "text-muted-foreground" : "text-secondary-foreground",
              )}
            >
              <TypeIcon className="size-4" /> Texto
            </button>
            <button
              type="button"
              onClick={() => {
                setFileMode(true);
                setError(null);
              }}
              className={cn(
                "relative z-10 flex items-center justify-center gap-2 rounded-xl py-2.5 text-sm font-medium transition-colors duration-300",
                fileMode ? "text-secondary-foreground" : "text-muted-foreground",
              )}
            >
              <FileUp className="size-4" /> Arquivo
            </button>
          </div>

          {fileMode ? (
            <div
              key="file"
              onClick={() => inputRef.current?.click()}
              className="animate-field-right flex cursor-pointer flex-col items-center justify-center rounded-2xl border-2 border-dashed border-secondary/40 bg-secondary/5 px-6 py-14 text-center transition-colors hover:bg-secondary/10"
            >
              <FileUp className="size-7 text-secondary" />
              <p className="mt-3 text-sm font-medium">
                {file ? file.name : "Clique para escolher um arquivo CSV ou TXT"}
              </p>
              <p className="mt-1 text-xs text-muted-foreground">
                Envie um arquivo com uma sequência de DNA por linha
              </p>
              <Input
                ref={inputRef}
                type="file"
                accept=".csv,.txt,text/csv,text/plain"
                className="hidden"
                onChange={(e) => setFile(e.target.files?.[0] ?? null)}
              />
            </div>
          ) : (
            <Textarea
              key="text"
              value={text}
              onChange={(e) => setText(e.target.value)}
              placeholder="ATGGCCATTGTAATGGGCCGCTGAAAGGGTGCCCGATAG..."
              className="animate-field-left min-h-44 resize-y font-mono text-sm tracking-wider"
            />
          )}

          {error && <p className="mt-4 text-sm text-destructive">{error}</p>}

          <Button
            variant="hero"
            size="lg"
            className="btn-shine mt-6 w-full transition-transform duration-200 hover:scale-[1.02] active:scale-[0.98]"
            onClick={handleAnalyze}
            disabled={loading}
          >
            <Play className="size-4 transition-transform duration-300 group-hover:translate-x-0.5" />
            {loading ? "Compilando..." : "Analisar"}
          </Button>
        </section>

      </main>
      {loading && <DnaLoaderOverlay label="Compilando sequência de DNA..." />}
    </div>
  );
}
