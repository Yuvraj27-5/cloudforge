/**
 * Structured JSON logs on stdout. Kubernetes collects stdout, and JSON is what
 * a log aggregator can query. Correlation IDs are propagated from the request so
 * a deployment is traceable from CloudForge through to this pod.
 */
export function log(level, message, fields = {}) {
  process.stdout.write(
    `${JSON.stringify({
      timestamp: new Date().toISOString(),
      level,
      service: "sample-app",
      message,
      ...fields,
    })}\n`,
  )
}

export const logger = {
  info: (message, fields) => log("INFO", message, fields),
  warn: (message, fields) => log("WARN", message, fields),
  error: (message, fields) => log("ERROR", message, fields),
}
