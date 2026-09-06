# prometheus

**Phase 10.** `prometheus.yml` scrape configuration and alert rules.

Alert rules that later drive rollback: sustained error rate above threshold, pod
restart count increasing, readiness probe failing, deployment not ready within its
timeout.
