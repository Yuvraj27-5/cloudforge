import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"

import { api } from "./client"
import type {
  CreateDeploymentRequest,
  Deployment,
  DeploymentDetail,
  DeploymentStatus,
} from "../types/deployment"
import type { PageResponse } from "../types/project"

const KEYS = {
  list: (projectId?: string) => ["deployments", { projectId: projectId ?? null }] as const,
  detail: (id: string) => ["deployments", id] as const,
}

export function useDeployments(projectId?: string) {
  return useQuery({
    queryKey: KEYS.list(projectId),
    queryFn: () =>
      api.get<PageResponse<Deployment>>(
        projectId ? `/deployments?projectId=${projectId}` : "/deployments",
      ),
  })
}

export function useDeployment(id: string) {
  return useQuery({
    queryKey: KEYS.detail(id),
    queryFn: () => api.get<DeploymentDetail>(`/deployments/${id}`),
  })
}

export function useCreateDeployment(projectId: string) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: CreateDeploymentRequest) =>
      api.post<Deployment>(`/projects/${projectId}/deployments`, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["deployments"] })
    },
  })
}

export function useUpdateDeploymentStatus(id: string) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (payload: { status: DeploymentStatus; reason?: string; actor?: string }) =>
      api.patch<DeploymentDetail>(`/deployments/${id}/status`, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["deployments"] })
    },
  })
}
