# Phase 6 — Security and Code Analysis

Two halves. CI measures, the backend stores. Those stored numbers are the risk
model's feature vector in Phase 7, so this phase is really about building the
feature pipeline.

## The central decision: null is not zero

`deployment_metrics` columns are all nullable, and the API treats absent as
"not measured".

Recording zero vulnerabilities because no scan ran would tell the Phase 7 model the
change was clean. That is the most dangerous possible failure for a risk engine —
it would confidently approve an unscanned deployment. Null propagates through to
the UI as an em dash rather than a reassuring zero.

Three tests exist purely to hold this line, including
`unmeasuredIsNotTreatedAsClean`.

## Scanning

Trivy runs as its **official container**, not through a third-party action, so
there is no action version to drift out of date. Two scans:

- `trivy image` — vulnerabilities in the built sample-app image
- `trivy fs --scanners vuln,secret,misconfig` — the repository itself

Two gates, deliberately different in kind:

**Any committed secret fails the build, unconditionally.** A leaked credential is
not a severity judgement.

**Critical image vulnerabilities fail the build.** High and below are reported in
the job summary and stored as features rather than blocking. A build that fails on
every medium CVE in a base image gets ignored within a week, and an ignored gate is
worse than no gate.

## Coverage

JaCoCo 0.8.14 — the first release with official Java 25 support. Coverage is parsed
from `jacoco.xml`, not scraped from console output, because Phase 7 uses the number
as a model input and it has to be exact. The extractor reads the report-level
`LINE` counter; reading the first `counter` element instead would silently return
one package's coverage.

## API

```
PUT /api/v1/deployments/{id}/metrics
```

PUT because it upserts one metrics record. A pipeline may report vulnerability
counts as soon as the scan finishes and coverage later; the second call updates the
same row rather than creating a duplicate.

`source` is required — "github-actions", "manual" — so a hand-entered number is
never mistaken for a measured one.

`hasBlockingVulnerabilities` is computed on the entity, not stored. Phase 8 uses it
as a hard gate independent of the model score: a critical CVE is not a probability
question.

## What CI does not yet do

The workflow writes `backend-metrics.json` and `security-metrics.json` as
artifacts. It does **not** POST them to the backend, because CI has no route to a
running CloudForge instance. Phase 8 closes that loop, when the pipeline calls the
decision engine and needs the metrics there anyway.

Static analysis beyond coverage — complexity, smells, bugs, security hotspots — has
columns and UI but no producer yet. SonarQube needs a server, and standing one up
adds infrastructure this phase does not need. The fields stay null, which the
system already handles correctly.

## Verification status

Verified by running: the jq severity counters against realistic and empty Trivy
output, the CRITICAL detail formatter, and the JaCoCo XML extractor against a report
containing both package-level and report-level counters. Frontend builds and lints
clean.

Not verified: the Trivy container invocations and the Docker socket mount, which
need a Docker daemon.
