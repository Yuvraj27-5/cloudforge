import assert from "node:assert/strict"
import { createServer } from "node:http"
import { after, before, beforeEach, describe, it } from "node:test"

import { handleRequest, state } from "../src/app.js"

let server
let baseUrl

async function get(path) {
  const response = await fetch(`${baseUrl}${path}`)
  return { status: response.status, body: await response.json(), headers: response.headers }
}

before(async () => {
  server = createServer((request, response) => {
    handleRequest(request, response)
  })

  await new Promise((resolve) => server.listen(0, resolve))
  baseUrl = `http://127.0.0.1:${server.address().port}`
})

after(() => server.close())

beforeEach(() => {
  state.healthy = true
  state.ready = true
})

describe("root", () => {
  it("reports which build is running", async () => {
    const { status, body } = await get("/")

    assert.equal(status, 200)
    assert.equal(body.service, "sample-app")
    assert.ok("commitSha" in body, "commitSha identifies the deployed build")
  })
})

describe("liveness", () => {
  it("is UP while healthy", async () => {
    const { status, body } = await get("/health")

    assert.equal(status, 200)
    assert.equal(body.status, "UP")
  })

  it("returns 503 once the failure mode trips", async () => {
    state.healthy = false

    const { status, body } = await get("/health")

    // 503 is what Kubernetes probes and CloudForge's rollback watcher key off.
    assert.equal(status, 503)
    assert.equal(body.status, "DOWN")
  })
})

describe("readiness", () => {
  it("is independent of liveness", async () => {
    state.ready = false
    state.healthy = true

    const ready = await get("/ready")
    const health = await get("/health")

    // Not ready must not imply not alive, or a slow start becomes a restart loop.
    assert.equal(ready.status, 503)
    assert.equal(health.status, 200)
  })
})

describe("correlation id", () => {
  it("echoes the caller's id so a deployment stays traceable", async () => {
    const response = await fetch(`${baseUrl}/health`, {
      headers: { "x-correlation-id": "cloudforge-test-123" },
    })

    assert.equal(response.headers.get("x-correlation-id"), "cloudforge-test-123")
  })

  it("generates one when the caller sends none", async () => {
    const { headers } = await get("/health")

    assert.ok(headers.get("x-correlation-id"))
  })
})

describe("unknown routes", () => {
  it("returns 404 rather than an unhandled error", async () => {
    const { status } = await get("/does-not-exist")

    assert.equal(status, 404)
  })
})
