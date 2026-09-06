import { useEffect, useState } from "react"

type SystemInfo = {
  service: string
  version: string
  phase: string
  timestamp: string
}

type Status = "loading" | "ok" | "error"

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? "/api/v1"

/**
 * Phase 0 connectivity probe, not a design. Real UI starts in Phase 1.
 */
export default function App() {
  const [status, setStatus] = useState<Status>("loading")
  const [info, setInfo] = useState<SystemInfo | null>(null)
  const [error, setError] = useState("")

  useEffect(() => {
    fetch(`${API_BASE}/system/info`)
      .then((res) => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        return res.json() as Promise<SystemInfo>
      })
      .then((data) => {
        setInfo(data)
        setStatus("ok")
      })
      .catch((err: unknown) => {
        setError(err instanceof Error ? err.message : "Unknown error")
        setStatus("error")
      })
  }, [])

  return (
    <main style={{ fontFamily: "system-ui, sans-serif", padding: "2rem", maxWidth: "40rem" }}>
      <h1>CloudForge</h1>
      <p>Multi-cloud CI/CD and deployment risk management. Phase 0.</p>
      <hr />
      <h2>Backend connectivity</h2>
      {status === "loading" && <p>Checking…</p>}
      {status === "error" && (
        <p style={{ color: "crimson" }}>
          Backend unreachable: {error}. Start it with <code>./mvnw spring-boot:run</code> in{" "}
          <code>backend/</code>.
        </p>
      )}
      {status === "ok" && info && (
        <ul>
          <li>Service: {info.service}</li>
          <li>Version: {info.version}</li>
          <li>Phase: {info.phase}</li>
          <li>Server time: {new Date(info.timestamp).toLocaleString()}</li>
        </ul>
      )}
    </main>
  )
}
