type Props = {
  kind: "loading" | "error" | "empty"
  children: React.ReactNode
}

const COLORS: Record<Props["kind"], string> = {
  loading: "var(--muted)",
  error: "var(--danger)",
  empty: "var(--muted)",
}

export default function StatusMessage({ kind, children }: Props) {
  return (
    <p role={kind === "error" ? "alert" : undefined} style={{ color: COLORS[kind] }}>
      {children}
    </p>
  )
}
