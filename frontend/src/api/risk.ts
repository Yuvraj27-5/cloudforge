import { useMutation, useQueryClient } from "@tanstack/react-query"

import { api } from "./client"
import type { RiskAssessment } from "../types/risk"

export function useAssessDeployment(id: string) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: () => api.post<RiskAssessment>(`/deployments/${id}/risk-assessment`, {}),
    onSuccess: () => {
      // The assessment changes which status transitions are allowed, so the
      // deployment must be refetched too.
      queryClient.invalidateQueries({ queryKey: ["deployments"] })
    },
  })
}
