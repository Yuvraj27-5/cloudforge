/**
 * Configuration from the environment. No defaults that would be wrong in
 * production, and no secrets.
 */

const FAILURE_MODES = ["none", "unhealthy", "crash", "slow"]

function readFailureMode() {
  const mode = process.env.FAILURE_MODE ?? "none"

  if (!FAILURE_MODES.includes(mode)) {
    // Fail loudly at startup rather than behaving unpredictably later.
    throw new Error(
      `FAILURE_MODE must be one of ${FAILURE_MODES.join(", ")} (received "${mode}")`,
    )
  }

  return mode
}

function readInt(name, fallback) {
  const raw = process.env[name]
  if (raw === undefined || raw === "") return fallback

  const value = Number.parseInt(raw, 10)
  if (Number.isNaN(value) || value < 0) {
    throw new Error(`${name} must be a non-negative integer (received "${raw}")`)
  }
  return value
}

export const config = {
  port: readInt("PORT", 3000),
  version: process.env.APP_VERSION ?? "1.0.0",
  commitSha: process.env.COMMIT_SHA ?? "unknown",

  /**
   * none      healthy for ever
   * unhealthy /health starts returning 503 after failureDelaySeconds
   * crash     the process exits, producing CrashLoopBackOff in Kubernetes
   * slow      every response is delayed, driving up latency and error rate
   */
  failureMode: readFailureMode(),
  failureDelaySeconds: readInt("FAILURE_DELAY_SECONDS", 30),
  slowResponseMs: readInt("SLOW_RESPONSE_MS", 2000),
}
