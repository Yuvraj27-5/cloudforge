import { STATUS_LABELS, type DeploymentEvent } from "../types/deployment"

/**
 * The audit trail, oldest first. This is a genuine sequence, which is what earns
 * the connected markers.
 */
export default function DeploymentTimeline({ events }: { events: DeploymentEvent[] }) {
  if (events.length === 0) {
    return <p className="muted">No events recorded.</p>
  }

  return (
    <ol className="timeline">
      {events.map((event) => (
        <li key={event.id}>
          <div className="timeline-head">
            <span className="timeline-label">
              {event.eventType === "CREATED"
                ? "Recorded"
                : `${event.fromStatus ? STATUS_LABELS[event.fromStatus] : "—"} to ${
                    event.toStatus ? STATUS_LABELS[event.toStatus] : "—"
                  }`}
            </span>
            <time className="mono">{new Date(event.occurredAt).toLocaleString()}</time>
          </div>
          {event.reason && <p className="timeline-reason">{event.reason}</p>}
          {event.actor && <span className="timeline-actor">by {event.actor}</span>}
        </li>
      ))}
    </ol>
  )
}
