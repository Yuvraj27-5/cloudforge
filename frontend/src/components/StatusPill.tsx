import { STATUS_LABELS, STATUS_TONE, type DeploymentStatus } from "../types/deployment"

export default function StatusPill({ status }: { status: DeploymentStatus }) {
  return <span className={`status status-${STATUS_TONE[status]}`}>{STATUS_LABELS[status]}</span>
}
