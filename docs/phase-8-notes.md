# Phase 8 — Deployment Decision Engine

Where a score becomes an action. CloudForge stops observing and starts refusing.

## Three policy decisions, and why

These are arguable. They are written as tests in `DecisionEngineTest` so changing
them is deliberate rather than accidental.

### 1. Bands stay at 0–30 / 31–70 / 71–100

The Phase 7 model catches roughly 23% of failures at a 0.5 threshold. Lowering the
bands would gate more deployments and catch more failures — and produce far more
false alarms.

A gate that fires constantly gets clicked through, and then it protects nothing. The
bands stay where they are, and the limitation is documented rather than hidden
behind a tuning knob. When real training data replaces synthetic, recall is the
number to revisit — not the thresholds.

### 2. A critical vulnerability blocks regardless of score

Evaluated **before** the model output, in `DecisionEngine.decide`.

Shipping a known critical CVE is not a probability question. The model predicts
deployment *failure*, which is a different risk — a vulnerable service can run
perfectly and still be a serious problem. Asking one model to express both would
corrupt what it is genuinely good at.

Note what this is not: `null` critical vulnerabilities does **not** trigger the gate.
Null means no scan ran, and that is handled upstream by refusing to score at all.

### 3. An unreachable risk engine fails closed

`decideWithoutScore()` returns `BLOCK_DEPLOYMENT`.

This is inconvenient every time the ML service is not running, and that is the
correct trade. The alternative is a platform that silently stops checking anything
and keeps saying yes — indistinguishable, from the outside, from one that is
working.

The assessment is still stored, with `riskScore` null and an override reason naming
the failure. Null score, not zero: a score of zero would read as "very safe".

## Separation of concerns

The ML service returns its own `decision` field. **The backend ignores it.**

Scoring and deciding are different jobs. The model produces a probability; the policy
that turns that into an action belongs to CloudForge and has to be changeable
without redeploying a model. Thresholds live in `application.yml`.

`RiskAssessmentProvider` is the seam. `DecisionEngine` never imports anything from
the ML client, so swapping the provider — heuristic, hosted service, a different
model — touches one class.

## Enforcement

`requireApprovalToStart` runs inside `DeploymentService.updateStatus`, not in a
controller, so every caller passes through it: the UI, the pipeline, a future CLI.

**No assessment means refusal.** A deployment nobody scored has not been approved.
That is the difference between a gate and a suggestion.

Only `APPROVE_DEPLOYMENT` allows a start. `EXTRA_VALIDATION` deliberately does not —
the human approval workflow that unblocks it is not built yet, and defaulting to
"allow" while it is missing would quietly make the medium band meaningless.

## Provenance

Every stored assessment records `modelVersion`, `trainedOn` and `provider`.

Without those, a score from a synthetic model is indistinguishable from a validated
one six months later. `trainedOn: "synthetic"` is surfaced all the way into the UI,
under the score.

Assessments are append-only. A deployment can be rescored after its metrics change,
and the earlier verdict survives so "why was this blocked at 2am" stays answerable.

## previous_deployment_failures

Counted from the project's own history by the backend, not reported by CI. The
pipeline cannot know what happened on earlier deployments; CloudForge can. Counting
`FAILED` and `ROLLED_BACK` is why Phase 2 kept those as distinct statuses.

## API

| Method | Path | Notes |
|---|---|---|
| POST | `/api/v1/deployments/{id}/risk-assessment` | Scores from stored metrics, stores the verdict |
| GET | `/api/v1/deployments/{id}/risk-assessment` | Latest, or 204 |

422 when metrics are missing or incomplete. 409 when a status change is refused by
policy — the request was valid, the deployment exists, and the answer is still no.

## Verified

Eight decision-engine policy tests. Frontend builds and lints clean.

Not verified: the ML service call path, which needs both services running.
