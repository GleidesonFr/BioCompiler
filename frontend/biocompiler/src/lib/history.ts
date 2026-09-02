import { useCallback, useEffect, useState } from "react";
import { API, PaginatedResponse } from "./api";
import type { BackendAnalysis } from "./dna";

export function useHistory(page = 0, size = 10) {
  const [records, setRecords] = useState<PaginatedResponse<BackendAnalysis>>({
    content: [],
    totalElements: 0,
    totalPages: 0,
    size: 0,
    number: 0,
    first: false,
    last: false,
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const sync = useCallback(async (nextPage = page, nextSize = size) => {
    try {
      setError(null);
      setLoading(true);
      setRecords(await API.getHistory(nextPage, nextSize));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Não foi possível carregar o histórico.");
    } finally {
      setLoading(false);
    }
  }, [page, size]);

  useEffect(() => {
    void sync(page, size);
  }, [page, size, sync]);

  return { records, loading, error, refresh: sync };
}
