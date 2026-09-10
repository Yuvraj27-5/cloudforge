type Props = {
  children: React.ReactNode
  /** Production gets a warmer border: it is the environment worth noticing. */
  emphasis?: "production"
}

export default function Badge({ children, emphasis }: Props) {
  return <span className={emphasis ? `badge ${emphasis}` : "badge"}>{children}</span>
}
