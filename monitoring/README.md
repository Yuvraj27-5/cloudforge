# monitoring

**Phase 10.** Prometheus scrapes `/actuator/prometheus` from the backend and the
sample app; Grafana renders it.

Phase 11 consumes these same signals for rollback decisions — pod restart count,
error rate, response time, readiness. The rollback trigger reads the metrics that
already exist rather than inventing a parallel health system.
