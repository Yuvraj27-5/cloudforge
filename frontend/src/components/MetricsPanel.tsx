import type { DeploymentMetrics } from "../types/deployment"

/** "—" rather than 0: not measured is not the same as measured as zero. */
function value(n: number | null, suffix = ""): string {
  return n === null || n === undefined ? "—" : `${n}${suffix}`
}

function severityTone(count: number | null, warnAt: number): string {
  if (count === null || count === undefined) return "neutral"
  if (count === 0) return "good"
  return count >= warnAt ? "bad" : "warn"
}

export default function MetricsPanel({ metrics }: { metrics: DeploymentMetrics | null }) {
  if (!metrics) {
    return (
      <div className="upcoming">
        <h3>Pipeline metrics</h3>
        <p>
          Nothing reported yet. CI records change size, coverage and vulnerability counts
          against a deployment; the risk engine scores them in Phase 7.
        </p>
      </div>
    )
  }

  return (
    <div className="panel">
      {metrics.hasBlockingVulnerabilities && (
        <div className="alert error" role="alert">
          Critical vulnerabilities present. Phase 8 will block this deployment regardless of
          its risk score.
        </div>
      )}

      <div className="metric-grid">
        <div className="metric">
          <span className="metric-label">Change size</span>
          <span className="metric-value">
            {value(metrics.filesChanged)} <span className="metric-unit">files</span>
          </span>
          <span className="subtle">
            +{value(metrics.linesAdded)} / −{value(metrics.linesDeleted)}
          </span>
        </div>

        <div className="metric">
          <span className="metric-label">Test coverage</span>
          <span className="metric-value">{value(metrics.testCoverage, "%")}</span>
          <span className="subtle">Pass rate {value(metrics.testPassRate, "%")}</span>
        </div>

        <div className="metric">
          <span className="metric-label">Complexity</span>
          <span className="metric-value">{value(metrics.codeComplexity)}</span>
          <span className="subtle">
            {value(metrics.codeSmells)} smells, {value(metrics.bugs)} bugs
          </span>
        </div>

        <div className="metric">
          <span className="metric-label">Vulnerabilities</span>
          <span className="metric-value">
            <span className={`sev sev-${severityTone(metrics.criticalVulnerabilities, 1)}`}>
              {value(metrics.criticalVulnerabilities)}
            </span>
            <span className="metric-unit"> critical</span>
          </span>
          <span className="subtle">
            {value(metrics.highVulnerabilities)} high, {value(metrics.mediumVulnerabilities)} medium,{" "}
            {value(metrics.lowVulnerabilities)} low
          </span>
        </div>
      </div>

      <p className="subtle">
        Reported by {metrics.source} at {new Date(metrics.recordedAt).toLocaleString()}
      </p>
    </div>
  )
}
