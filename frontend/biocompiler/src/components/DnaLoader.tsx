import { cn } from "@/lib/utils";

const SEGMENTS = 14;

export function DnaHelix({ className }: { className?: string }) {
  return (
    <div className={cn("flex items-center gap-[5px]", className)} aria-hidden>
      {Array.from({ length: SEGMENTS }).map((_, i) => (
        <div key={i} className="flex flex-col items-center gap-[3px]">
          <span
            className="dna-dot block size-[6px] rounded-full bg-secondary"
            style={{ animationDelay: `${i * 90}ms` }}
          />
          <span
            className="dna-bar block h-[14px] w-[2px] rounded-full bg-secondary/40"
            style={{ animationDelay: `${i * 90}ms` }}
          />
          <span
            className="dna-dot dna-dot-b block size-[6px] rounded-full bg-secondary"
            style={{ animationDelay: `${i * 90}ms` }}
          />
        </div>
      ))}
    </div>
  );
}

export function DnaLoader({ label = "Carregando...", className }: { label?: string; className?: string }) {
  return (
    <div className={cn("flex flex-col items-center justify-center gap-4", className)} aria-live="polite">
      <DnaHelix />
      <p className="text-sm font-medium text-muted-foreground">{label}</p>
    </div>
  );
}

export function DnaLoaderOverlay({ label = "Carregando..." }: { label?: string }) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-background/80 backdrop-blur-sm animate-fade-in">
      <DnaLoader label={label} />
    </div>
  );
}