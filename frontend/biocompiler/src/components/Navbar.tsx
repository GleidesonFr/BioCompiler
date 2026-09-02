import { Link, useNavigate } from "@tanstack/react-router";
import { Dna, History, Moon, Sun } from "lucide-react";
import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";

export function Navbar() {
  const [dark, setDark] = useState(false);
  const navigate = useNavigate();

  const goToHistory = () => {
    void navigate({ to: "/historico" });
  };

  useEffect(() => {
    const stored = window.localStorage.getItem("biocompiler.theme");
    const isDark = stored === "dark";
    setDark(isDark);
    document.documentElement.classList.toggle("dark", isDark);
  }, []);

  const toggle = () => {
    const next = !dark;
    setDark(next);
    document.documentElement.classList.toggle("dark", next);
    window.localStorage.setItem("biocompiler.theme", next ? "dark" : "light");
  };

  return (
    <header className="sticky top-0 z-40 border-b border-border/70 bg-background/80 backdrop-blur-md">
      <nav className="mx-auto flex h-16 w-full max-w-6xl items-center justify-between gap-4 px-5">
        <Link to="/" className="group flex items-center gap-2.5">
          <span className="flex size-9 items-center justify-center rounded-xl border border-secondary/40 bg-gradient-to-br from-secondary/20 via-primary/10 to-ok/15 text-secondary shadow-glow">
            <Dna className="size-5" />
          </span>
          <span className="font-display text-lg font-semibold tracking-tight">
            <span className="text-secondary">Bio</span>Compiler <span className="text-secondary">1.0</span>
          </span>
        </Link>

        <div className="flex items-center gap-2">
          <Button
            variant="brand"
            size="icon"
            onClick={toggle}
            aria-label={dark ? "Ativar modo claro" : "Ativar modo escuro"}
          >
            {dark ? <Sun className="size-4" /> : <Moon className="size-4" />}
          </Button>
          <Button variant="brand" onClick={goToHistory}>
            <History className="size-4" />
            Histórico
          </Button>
        </div>
      </nav>
    </header>
  );
}
