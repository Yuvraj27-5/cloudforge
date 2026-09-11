export type RiskLevel = "LOW" | "MEDIUM" | "HIGH"

export type DeploymentDecision =
  | "APPROVE_DEPLOYMENT"
  | "EXTRA_VALIDATION"
  | "BLOCK_DEPLOYMENT"

export type RiskFactor = {
  feature: string
  value: number
  contribution: number
  explanation: string
}

export type RiskAssessment = {
  id: string
  /** Null when no score was obtained. Never treat as zero. */
  riskScore: number | null
  riskLevel: RiskLevel | null
  decision: DeploymentDecision
  confidence: number | null
  factors: RiskFactor[]
  modelVersion: string | null
  /** "synthetic" means encoded assumptions, not observed outcomes. */
  trainedOn: string | null
  provider: string
  overrideReason: string | null
  allowsDeployment: boolean
  assessedAt: string
}

export const DECISION_LABELS: Record<DeploymentDecision, string> = {
  APPROVE_DEPLOYMENT: "Approved",
  EXTRA_VALIDATION: "Needs validation",
  BLOCK_DEPLOYMENT: "Blocked",
}

export const LEVEL_TONE: Record<RiskLevel, string> = {
  LOW: "good",
  MEDIUM: "warn",
  HIGH: "bad",
}
