type Props = {
  label: string
  htmlFor: string
  error?: string
  children: React.ReactNode
}

export default function Field({ label, htmlFor, error, children }: Props) {
  return (
    <div className="field">
      <label htmlFor={htmlFor}>{label}</label>
      {children}
      {error && (
        <span className="field-error" role="alert">
          {error}
        </span>
      )}
    </div>
  )
}
