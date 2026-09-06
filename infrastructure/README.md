# infrastructure

Deployment manifests and cloud provisioning. Nothing here is used before Phase 9.

| Path | Phase | Contents |
|---|---|---|
| `kubernetes/base` | 9 | Deployment, Service, ConfigMap, probes — provider-neutral |
| `kubernetes/overlays/local` | 9 | kind/Minikube overlay |
| `kubernetes/overlays/aws` | 12 | EKS overlay, ECR image references |
| `kubernetes/overlays/azure` | 13 | AKS overlay, ACR image references |
| `aws/` | 12 | ECR + EKS provisioning, IAM policy documents |
| `azure/` | 13 | ACR + AKS provisioning, RBAC role assignments |

Secrets are never written into manifests. Kubernetes Secrets are referenced by name
and populated from AWS Secrets Manager or Azure Key Vault.
