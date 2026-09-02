import type { BackendAnalysis } from "./dna";

const API_BASE_URL = (import.meta.env["VITE_API_BASE_URL"] ?? "").replace(/\/$/, "");
const SESSION_STORAGE_KEY = "biocompiler_session_id";

function generateSessionId(): string {
  return crypto.randomUUID();
}

export function getSessionId(): string {
  const stored = sessionStorage.getItem(SESSION_STORAGE_KEY);
  if (stored) return stored;

  const nextSessionId = generateSessionId();
  sessionStorage.setItem(SESSION_STORAGE_KEY, nextSessionId);
  return nextSessionId;
}

export function resetSessionId(): void {
  sessionStorage.removeItem(SESSION_STORAGE_KEY);
  const nextSessionId = generateSessionId();
  sessionStorage.setItem(SESSION_STORAGE_KEY, nextSessionId);
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface HistoryStats {
  correct: number;
  invalidBase: number;
  startMissing: number;
  stopMissing: number;
  frameshift: number;
  nonsense: number;
}

async function parseResponse<T>(response: Response): Promise<T> {
  const contentType = response.headers.get("content-type") ?? "";
  const data = contentType.includes("application/json")
    ? await response.json()
    : await response.text();

  if (!response.ok) {
    const message =
      typeof data === "object" && data !== null && "message" in data
        ? String((data as { message: unknown }).message)
        : typeof data === "string" && data.trim()
          ? data
          : "Erro ao comunicar com o BioCompiler.";
    throw new Error(message);
  }

  return data as T;
}

export const API = {
  async analyzeSequence(sequence: string): Promise<BackendAnalysis> {
    const sessionId = getSessionId();
    const response = await fetch(`${API_BASE_URL}/api/analysis`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ sequence, sessionId }),
    });
    return parseResponse<BackendAnalysis>(response);
  },

  async analyzeFile(file: File): Promise<BackendAnalysis[]> {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("sessionId", getSessionId());

    const response = await fetch(`${API_BASE_URL}/api/analysis/file`, {
      method: "POST",
      body: formData,
    });
    return parseResponse<BackendAnalysis[]>(response);
  },

  async getHistory(page = 0, size = 10): Promise<PaginatedResponse<BackendAnalysis>> {
    const params = new URLSearchParams({ page: String(page), size: String(size), sessionId: getSessionId() });
    const response = await fetch(`${API_BASE_URL}/api/analysis/history?${params}`, {
      headers: { Accept: "application/json" },
    });
    return parseResponse<PaginatedResponse<BackendAnalysis>>(response);
  },

  async getHistoryStats(): Promise<HistoryStats> {
    const params = new URLSearchParams({ sessionId: getSessionId() });
    const response = await fetch(`${API_BASE_URL}/api/analysis/history/stats?${params}`, {
      headers: { Accept: "application/json" },
    });
    return parseResponse<HistoryStats>(response);
  },

  async getAnalysis(id: string): Promise<BackendAnalysis> {
    const response = await fetch(`${API_BASE_URL}/api/analysis/${encodeURIComponent(id)}`, {
      headers: { Accept: "application/json" },
    });
    return parseResponse<BackendAnalysis>(response);
  },

  async clearHistory(): Promise<void> {
    const params = new URLSearchParams({ sessionId: getSessionId() });
    const response = await fetch(`${API_BASE_URL}/api/analysis/history?${params}`, {
      method: "DELETE",
    });
    await parseResponse<unknown>(response);
  },
};
