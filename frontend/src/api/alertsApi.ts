import apiClient from './apiClient';
import type { Alert, CreateAlertRequest } from '@/types/alert';

/**
 * Fetches all alerts configured by the current user.
 * @returns list of user alerts with their configuration and status
 */
export async function fetchAlerts(): Promise<Alert[]> {
  const response = await apiClient.get<Alert[]>('/alerts');
  return response.data;
}

/**
 * Creates a new alert for a stock symbol.
 * @param request - the alert configuration (symbol, type, threshold, frequency)
 * @returns the newly created alert
 */
export async function createAlert(request: CreateAlertRequest): Promise<Alert> {
  const response = await apiClient.post<Alert>('/alerts', request);
  return response.data;
}

/**
 * Toggles an alert between active and inactive states.
 * @param alertId - the ID of the alert to toggle
 * @returns the updated alert with new active status
 */
export async function toggleAlert(alertId: number): Promise<Alert> {
  const response = await apiClient.patch<Alert>(`/alerts/${alertId}/toggle`);
  return response.data;
}

/**
 * Permanently deletes an alert.
 * @param alertId - the ID of the alert to delete
 */
export async function deleteAlert(alertId: number): Promise<void> {
  await apiClient.delete(`/alerts/${alertId}`);
}
