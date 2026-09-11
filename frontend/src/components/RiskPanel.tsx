import { DECISION_LABELS, LEVEL_TONE, type RiskAssessment } from "../types/risk"

type Props = {
  assessment: RiskAssessment | null
  onAssess: () => void
  isAssessing: boolean
  canAssess: boolean
}

export default function RiskPanel({ assessment, onAssess, isAssessing, canAssess }: Props) {
  if (!assessment) {
    return (
      <div className="upcoming">
        <h3>Not yet assessed</h3>
        <p>
          {canAssess
            ? "Score this deployment against the risk engine. It cannot start until it has been assessed and approved."
            : "Record pipeline metrics first. A deployment cannot be scored on numbers nobody measured."}
        </p>
        <button onClick={onAssess} disabled={!canAssess || isAssessing}>
          {isAssessing ? "Scoring…" : "Assess risk"}
        </button>
      </div>
    )
  }

  const tone = assessment.riskLevel ? LEVEL_TONE[assessment.riskLevel] : "neutral"

  return (
    <div className="panel">
      <div className="risk-head">
        <div className="risk-score-block">
          <span className={`risk-score sev-${tone}`}>
            {assessment.riskScore === null ? "—" : assessment.riskScore}
          </span>
          <span className="metric-label">
            {assessment.riskLevel ?? "Not scored"} risk
          </span>
        </div>

        <div className="risk-verdict">
          <span className={`status status-${assessment.allowsDeployment ? "good" : "bad"}`}>
            {DECISION_LABELS[assessment.decision]}
          </span>
          {assessment.confidence !== null && (
            <span className="subtle">
              Model confidence {(assessment.confidence * 100).toFixed(0)}%
            </span>
          )}
        </div>
      </div>

      {assessment.overrideReason && (
        <div className="alert error" role="alert">
          {assessment.overrideReason}
        </div>
      )}

      {assessment.trainedOn === "synthetic" && (
        <p className="subtle synthetic-warning">
          Scored by a model trained on synthetic data. The number reflects encoded
          assumptions, not observed production outcomes.
        </p>
      )}

      {assessment.factors.length > 0 && (
        <>
          <h3>What drove this score</h3>
          <ul className="factor-list">
            {assessment.factors.map((factor) => (
              <li key={factor.feature}>
                <span
                  className={`factor-bar ${factor.contribution > 0 ? "up" : "down"}`}
                  style={{ width: `${Math.min(Math.abs(factor.contribution) * 4, 100)}%` }}
                />
                <span className="factor-text">{factor.explanation}</span>
                <span className={`factor-value ${factor.contribution > 0 ? "sev-bad" : "sev-good"}`}>
                  {factor.contribution > 0 ? "+" : ""}
                  {factor.contribution.toFixed(1)}
                </span>
              </li>
            ))}
          </ul>
        </>
      )}

      <p className="subtle">
        {assessment.provider}
        {assessment.modelVersion && ` · model ${assessment.modelVersion}`} ·{" "}
        {new Date(assessment.assessedAt).toLocaleString()}
      </p>

      <div className="button-row">
        <button className="secondary" onClick={onAssess} disabled={isAssessing}>
          {isAssessing ? "Rescoring…" : "Rescore"}
        </button>
      </div>
    </div>
  )
}
