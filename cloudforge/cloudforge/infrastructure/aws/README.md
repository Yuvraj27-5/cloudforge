# aws

**Phase 12.** ECR repository, EKS cluster access, and IAM policies.

Least privilege: the pipeline role may push to one repository and update one
deployment. No static access keys — GitHub Actions authenticates via OIDC, pods via
IAM Roles for Service Accounts.
