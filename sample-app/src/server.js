import { createServer } from "node:http"

import { armFailureMode, handleRequest, state } from "./app.js"
import { config } from "./config.js"
import { logger } from "./logger.js"

const server = createServer((request, response) => {
  handleRequest(request, response).catch((error) => {
    logger.error("Unhandled request error", { error: error.message })
    response.writeHead(500, { "content-type": "application/json" })
    response.end(JSON.stringify({ error: "Internal server error" }))
  })
})

server.listen(config.port, () => {
  state.ready = true

  logger.info("Server listening", {
    port: config.port,
    version: config.version,
    commitSha: config.commitSha,
    failureMode: config.failureMode,
  })

  armFailureMode()
})

/**
 * Kubernetes sends SIGTERM and waits before SIGKILL. Draining in-flight requests
 * here is what makes a rolling update or a rollback avoid dropped connections.
 */
function shutdown(signal) {
  logger.info("Shutting down", { signal })
  state.ready = false

  server.close(() => {
    logger.info("Shutdown complete")
    process.exit(0)
  })

  setTimeout(() => {
    logger.warn("Forcing shutdown after timeout")
    process.exit(1)
  }, 10_000).unref()
}

process.on("SIGTERM", () => shutdown("SIGTERM"))
process.on("SIGINT", () => shutdown("SIGINT"))
