import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { fetchAlerts, createAlert, toggleAlert, deleteAlert } from '@/api/alertsApi';
import type { CreateAlertRequest } from '@/types/alert';

/** Query key factory for alert-related queries. */
const alertKeys = {
  all: ['alerts'] as const,
};

/**
 * Fetches all alerts for the current user.
 * Stale time: 30 seconds (alerts change with user interaction).
 */
export function useAlerts() {
  return useQuery({
    queryKey: alertKeys.all,
    queryFn: fetchAlerts,
    staleTime: 30_000,
  });
}

/**
 * Mutation to create a new alert.
 * Invalidates the alerts query on success.
 */
export function useCreateAlert() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: CreateAlertRequest) => createAlert(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: alertKeys.all });
    },
  });
}

/**
 * Mutation to toggle an alert's active state.
 * Invalidates the alerts query on success.
 */
export function useToggleAlert() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (alertId: number) => toggleAlert(alertId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: alertKeys.all });
    },
  });
}

/**
 * Mutation to permanently delete an alert.
 * Invalidates the alerts query on success.
 */
export function useDeleteAlert() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (alertId: number) => deleteAlert(alertId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: alertKeys.all });
    },
  });
}
