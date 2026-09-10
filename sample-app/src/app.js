import { randomUUID } from "node:crypto"

import { config } from "./config.js"
import { logger } from "./logger.js"

const startedAt = Date.now()

/**
 * Health state lives here rather than in the request handler so tests can drive
 * it directly without waiting out real timers.
 */
export const state = {
  healthy: true,
  ready: false,
}

function uptimeSeconds() {
  return Math.floor((Date.now() - startedAt) / 1000)
}

function send(response, status, body, correlationId) {
  const payload = JSON.stringify(body)

  response.writeHead(status, {
    "content-type": "application/json",
    "content-length": Buffer.byteLength(payload),
    "x-correlation-id": correlationId,
  })
  response.end(payload)
}

const routes = {
  "/": () => ({
    status: 200,
    body: {
      service: "sample-app",
      message: "Deployed by CloudForge",
      version: config.version,
      commitSha: config.commitSha,
      uptimeSeconds: uptimeSeconds(),
    },
  }),

  /** Liveness. Failing this restarts the pod. */
  "/health": () =>
    state.healthy
      ? { status: 200, body: { status: "UP", uptimeSeconds: uptimeSeconds() } }
      : {
          status: 503,
          body: {
            status: "DOWN",
            reason: "Simulated failure",
            failureMode: config.failureMode,
            uptimeSeconds: uptimeSeconds(),
          },
        },

  /**
   * Readiness. Failing this only removes the pod from the load balancer.
   * Kept separate from liveness: conflating them means a slow-starting pod gets
   * killed in a restart loop instead of simply waiting.
   */
  "/ready": () =>
    state.ready && state.healthy
      ? { status: 200, body: { status: "READY" } }
      : { status: 503, body: { status: "NOT_READY" } },
}

export async function handleRequest(request, response) {
  const correlationId = request.headers["x-correlation-id"] ?? randomUUID()
  const path = new URL(request.url, "http://localhost").pathname

  if (config.failureMode === "slow") {
    await new Promise((resolve) => setTimeout(resolve, config.slowResponseMs))
  }

  const route = routes[path]

  if (!route) {
    logger.warn("Route not found", { path, correlationId })
    return send(response, 404, { error: "Not found", path }, correlationId)
  }

  const { status, body } = route()

  if (status >= 500) {
    logger.error("Request failed", { path, status, correlationId })
  } else {
    logger.info("Request served", { path, status, correlationId })
  }

  return send(response, status, body, correlationId)
}

/**
 * Arms whichever failure the environment asked for. Returns the timer so the
 * caller can clear it; nothing is scheduled in the default healthy mode.
 */
export function armFailureMode() {
  const { failureMode, failureDelaySeconds } = config

  if (failureMode === "none" || failureMode === "slow") {
    return null
  }

  logger.warn("Failure mode armed", { failureMode, failureDelaySeconds })

  return setTimeout(() => {
    if (failureMode === "unhealthy") {
      state.healthy = false
      logger.error("Health endpoint now failing", { failureMode })
      return
    }

    if (failureMode === "crash") {
      logger.error("Exiting to simulate a crash", { failureMode })
      process.exit(1)
    }
  }, failureDelaySeconds * 1000)
}
