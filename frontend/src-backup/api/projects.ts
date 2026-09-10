import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { useNavigate } from "react-router-dom"

import { api } from "./client"
import type { CreateProjectRequest, PageResponse, Project } from "../types/project"

const KEYS = {
  all: ["projects"] as const,
  detail: (id: string) => ["projects", id] as const,
}

export function useProjects() {
  return useQuery({
    queryKey: KEYS.all,
    queryFn: () => api.get<PageResponse<Project>>("/projects"),
  })
}

export function useProject(id: string) {
  return useQuery({
    queryKey: KEYS.detail(id),
    queryFn: () => api.get<Project>(`/projects/${id}`),
  })
}

export function useCreateProject() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: CreateProjectRequest) => api.post<Project>("/projects", request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: KEYS.all })
    },
  })
}

export function useDeleteProject() {
  const queryClient = useQueryClient()
  const navigate = useNavigate()

  return useMutation({
    mutationFn: (id: string) => api.delete(`/projects/${id}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: KEYS.all })
      navigate("/projects")
    },
  })
}
