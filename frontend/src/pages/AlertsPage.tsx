import { useState } from 'react';
import {
  Bell,
  Plus,
  Trash2,
  TrendingUp,
  AlertTriangle,
  Newspaper,
  Loader2,
  AlertCircle,
  X,
  Clock,
} from 'lucide-react';
import { useAlerts, useCreateAlert, useToggleAlert, useDeleteAlert } from '@/hooks/useAlerts';
import { formatRelativeTime } from '@/utils';
import type { Alert, AlertType, AlertFrequency, AlertSentiment, CreateAlertRequest } from '@/types/alert';

/** Human-readable labels for alert types. */
const ALERT_TYPE_LABELS: Record<AlertType, string> = {
  NEWS_VOLUME: 'News Volume',
  SENTIMENT_CHANGE: 'Sentiment Change',
  BREAKING_NEWS: 'Breaking News',
};

/** Human-readable labels for alert frequency values. */
const FREQUENCY_LABELS: Record<AlertFrequency, string> = {
  HOURLY: 'Hourly',
  DAILY: 'Daily',
  WEEKLY: 'Weekly',
};

/** Human-readable labels for sentiment values. */
const SENTIMENT_LABELS: Record<AlertSentiment, string> = {
  POSITIVE: 'Positive',
  NEGATIVE: 'Negative',
  NEUTRAL: 'Neutral',
};

/** Icons associated with each alert type. */
const ALERT_TYPE_ICONS: Record<AlertType, typeof TrendingUp> = {
  NEWS_VOLUME: TrendingUp,
  SENTIMENT_CHANGE: AlertTriangle,
  BREAKING_NEWS: Newspaper,
};

/**
 * Returns the appropriate detail string for an alert based on its type.
 * @param alert - the alert to describe
 * @returns a human-readable detail string
 */
function getAlertDetailText(alert: Alert): string {
  switch (alert.alertType) {
    case 'NEWS_VOLUME':
      return `Threshold: ${alert.thresholdValue ?? 0} articles`;
    case 'SENTIMENT_CHANGE':
      return `Target: ${alert.targetSentiment ? SENTIMENT_LABELS[alert.targetSentiment] : 'Any'}`;
    case 'BREAKING_NEWS':
      return 'Notifies on any breaking news';
  }
}

/**
 * Alerts management page where users can create, toggle, and delete
 * custom news and sentiment alerts for tracked stock symbols.
 */
export function AlertsPage() {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [alertIdToDelete, setAlertIdToDelete] = useState<number | null>(null);

  const { data: alerts, isLoading, isError, error, refetch } = useAlerts();
  const createMutation = useCreateAlert();
  const toggleMutation = useToggleAlert();
  const deleteMutation = useDeleteAlert();

  /** Confirms and executes alert deletion. */
  const handleConfirmDelete = () => {
    if (alertIdToDelete === null) return;
    deleteMutation.mutate(alertIdToDelete, {
      onSuccess: () => setAlertIdToDelete(null),
    });
  };

  return (
    <div>
      {/* Header */}
      <div className="mb-8 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">Alerts</h1>
          <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
            Set up custom notifications for news and sentiment changes
          </p>
        </div>
        <button
          onClick={() => setIsModalOpen(true)}
          className="flex w-full items-center justify-center gap-2 rounded-lg bg-primary-600 px-4 py-2.5 text-sm font-medium text-white transition-colors hover:bg-primary-700 sm:w-auto"
        >
          <Plus className="h-4 w-4" />
          Create Alert
        </button>
      </div>

      {/* Loading State */}
      {isLoading && (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 6 }).map((_, index) => (
            <div
              key={index}
              className="animate-pulse rounded-xl border border-gray-200 bg-white p-5 dark:border-gray-700 dark:bg-gray-800"
            >
              <div className="mb-3 h-5 w-20 rounded bg-gray-200 dark:bg-gray-700" />
              <div className="mb-2 h-4 w-32 rounded bg-gray-200 dark:bg-gray-700" />
              <div className="h-3 w-24 rounded bg-gray-200 dark:bg-gray-700" />
            </div>
          ))}
        </div>
      )}

      {/* Error State */}
      {isError && (
        <div className="flex flex-col items-center justify-center py-16">
          <AlertCircle className="mb-3 h-10 w-10 text-red-500" />
          <p className="text-sm text-red-600 dark:text-red-400">
            {(error as Error)?.message || 'Failed to load alerts'}
          </p>
          <button
            onClick={() => refetch()}
            className="mt-4 rounded-lg border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 transition-colors hover:bg-gray-50 dark:border-gray-600 dark:text-gray-300 dark:hover:bg-gray-700"
          >
            Retry
          </button>
        </div>
      )}

      {/* Empty State */}
      {!isLoading && !isError && alerts && alerts.length === 0 && (
        <div className="flex flex-col items-center justify-center rounded-xl border-2 border-dashed border-gray-300 py-16 dark:border-gray-700">
          <Bell className="mb-4 h-12 w-12 text-gray-400 dark:text-gray-600" />
          <h2 className="text-lg font-semibold text-gray-600 dark:text-gray-400">
            No alerts yet
          </h2>
          <p className="mt-2 max-w-md text-center text-sm text-gray-500 dark:text-gray-500">
            Create alerts for breaking news, sentiment thresholds, and watchlist activity.
          </p>
          <button
            onClick={() => setIsModalOpen(true)}
            className="mt-6 flex items-center gap-2 rounded-lg bg-primary-600 px-4 py-2.5 text-sm font-medium text-white transition-colors hover:bg-primary-700"
          >
            <Plus className="h-4 w-4" />
            Create your first alert
          </button>
        </div>
      )}

      {/* Alerts Grid */}
      {alerts && alerts.length > 0 && (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {alerts.map((alert) => (
            <AlertCard
              key={alert.id}
              alert={alert}
              isToggling={toggleMutation.isPending}
              onToggle={(id) => toggleMutation.mutate(id)}
              onDelete={(id) => setAlertIdToDelete(id)}
            />
          ))}
        </div>
      )}

      {/* Create Alert Modal */}
      {isModalOpen && (
        <CreateAlertModal
          onClose={() => setIsModalOpen(false)}
          onSubmit={(request) => {
            createMutation.mutate(request, {
              onSuccess: () => setIsModalOpen(false),
            });
          }}
          isSubmitting={createMutation.isPending}
          submitError={createMutation.isError ? (createMutation.error?.message || 'Failed to create alert') : null}
        />
      )}

      {/* Delete Confirmation Modal */}
      {alertIdToDelete !== null && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
          <div className="mx-4 w-full max-w-sm rounded-xl bg-white p-6 shadow-xl dark:bg-gray-800">
            <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">
              Delete this alert?
            </h3>
            <p className="mt-2 text-sm text-gray-500 dark:text-gray-400">
              This alert will be permanently removed. This action cannot be undone.
            </p>
            <div className="mt-5 flex justify-end gap-3">
              <button
                onClick={() => setAlertIdToDelete(null)}
                className="rounded-lg border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-gray-600 dark:text-gray-300 dark:hover:bg-gray-700"
              >
                Cancel
              </button>
              <button
                onClick={handleConfirmDelete}
                disabled={deleteMutation.isPending}
                className="flex items-center gap-2 rounded-lg bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700 disabled:opacity-50"
              >
                {deleteMutation.isPending && <Loader2 className="h-4 w-4 animate-spin" />}
                Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

/** Props for the AlertCard component. */
interface AlertCardProps {
  alert: Alert;
  isToggling: boolean;
  onToggle: (alertId: number) => void;
  onDelete: (alertId: number) => void;
}

/**
 * Displays a single alert as a card with symbol, type, details,
 * frequency badge, active toggle, and delete action.
 */
function AlertCard({ alert, isToggling, onToggle, onDelete }: AlertCardProps) {
  const TypeIcon = ALERT_TYPE_ICONS[alert.alertType];

  return (
    <div
      className={`group relative rounded-xl border bg-white p-5 transition-shadow hover:shadow-md dark:bg-gray-800 ${
        alert.isActive
          ? 'border-l-4 border-l-primary-500 border-t-gray-200 border-r-gray-200 border-b-gray-200 dark:border-t-gray-700 dark:border-r-gray-700 dark:border-b-gray-700'
          : 'border-l-4 border-l-gray-300 border-t-gray-200 border-r-gray-200 border-b-gray-200 dark:border-l-gray-600 dark:border-t-gray-700 dark:border-r-gray-700 dark:border-b-gray-700'
      }`}
    >
      {/* Top row: symbol + actions */}
      <div className="mb-3 flex items-center justify-between">
        <span className="inline-flex items-center rounded-lg bg-primary-100 px-3 py-1.5 text-sm font-bold text-primary-700 dark:bg-primary-900/30 dark:text-primary-400">
          {alert.symbol}
        </span>
        <div className="flex items-center gap-1">
          {/* Toggle Switch */}
          <button
            onClick={() => onToggle(alert.id)}
            disabled={isToggling}
            title={alert.isActive ? 'Deactivate alert' : 'Activate alert'}
            className={`relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out disabled:cursor-not-allowed disabled:opacity-50 ${
              alert.isActive ? 'bg-primary-600' : 'bg-gray-300 dark:bg-gray-600'
            }`}
          >
            <span
              className={`pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out ${
                alert.isActive ? 'translate-x-5' : 'translate-x-0'
              }`}
            />
          </button>

          {/* Delete Button */}
          <button
            onClick={() => onDelete(alert.id)}
            className="rounded-lg p-1.5 text-gray-400 opacity-0 transition-all hover:bg-red-50 hover:text-red-500 group-hover:opacity-100 dark:hover:bg-red-900/20 dark:hover:text-red-400"
            title="Delete alert"
          >
            <Trash2 className="h-4 w-4" />
          </button>
        </div>
      </div>

      {/* Alert type with icon */}
      <div className="mb-2 flex items-center gap-2">
        <TypeIcon className="h-4 w-4 text-gray-500 dark:text-gray-400" />
        <span className="text-sm font-medium text-gray-900 dark:text-gray-100">
          {ALERT_TYPE_LABELS[alert.alertType]}
        </span>
      </div>

      {/* Alert detail */}
      <p className="mb-3 text-xs text-gray-500 dark:text-gray-400">
        {getAlertDetailText(alert)}
      </p>

      {/* Bottom row: frequency + last triggered */}
      <div className="flex items-center justify-between text-xs text-gray-500 dark:text-gray-400">
        <span className="inline-flex items-center gap-1 rounded-full bg-gray-100 px-2.5 py-0.5 font-medium text-gray-600 dark:bg-gray-700 dark:text-gray-300">
          <Clock className="h-3 w-3" />
          {FREQUENCY_LABELS[alert.frequency]}
        </span>
        <span>
          {alert.lastTriggeredAt
            ? `Triggered ${formatRelativeTime(alert.lastTriggeredAt)}`
            : 'Never triggered'}
        </span>
      </div>
    </div>
  );
}

/** Props for the CreateAlertModal component. */
interface CreateAlertModalProps {
  onClose: () => void;
  onSubmit: (request: CreateAlertRequest) => void;
  isSubmitting: boolean;
  submitError: string | null;
}

/**
 * Modal overlay with a form to create a new alert.
 * Conditionally shows threshold or sentiment fields based on alert type.
 */
function CreateAlertModal({ onClose, onSubmit, isSubmitting, submitError }: CreateAlertModalProps) {
  const [symbol, setSymbol] = useState('');
  const [alertType, setAlertType] = useState<AlertType>('BREAKING_NEWS');
  const [thresholdValue, setThresholdValue] = useState('');
  const [targetSentiment, setTargetSentiment] = useState<AlertSentiment>('NEGATIVE');
  const [frequency, setFrequency] = useState<AlertFrequency>('DAILY');
  const [validationError, setValidationError] = useState<string | null>(null);

  /** Validates the form and submits if valid. */
  const handleSubmit = (event: React.FormEvent) => {
    event.preventDefault();
    setValidationError(null);

    const trimmedSymbol = symbol.trim().toUpperCase();
    if (!trimmedSymbol) {
      setValidationError('Symbol is required');
      return;
    }

    if (alertType === 'NEWS_VOLUME') {
      const parsedThreshold = Number(thresholdValue);
      if (!thresholdValue || isNaN(parsedThreshold) || parsedThreshold <= 0) {
        setValidationError('Threshold must be a positive number');
        return;
      }
    }

    const request: CreateAlertRequest = {
      symbol: trimmedSymbol,
      alertType,
      frequency,
    };

    if (alertType === 'NEWS_VOLUME') {
      request.thresholdValue = Number(thresholdValue);
    }

    if (alertType === 'SENTIMENT_CHANGE') {
      request.targetSentiment = targetSentiment;
    }

    onSubmit(request);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
      <div className="mx-4 w-full max-w-md rounded-xl bg-white p-6 shadow-xl dark:bg-gray-800">
        {/* Modal Header */}
        <div className="mb-5 flex items-center justify-between">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">
            Create Alert
          </h3>
          <button
            onClick={onClose}
            className="rounded-lg p-1.5 text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-600 dark:hover:bg-gray-700 dark:hover:text-gray-300"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Error Messages */}
        {(validationError || submitError) && (
          <div className="mb-4 rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-700 dark:border-red-800 dark:bg-red-900/20 dark:text-red-400">
            {validationError || submitError}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Symbol */}
          <div>
            <label htmlFor="alert-symbol" className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
              Symbol
            </label>
            <input
              id="alert-symbol"
              type="text"
              value={symbol}
              onChange={(e) => setSymbol(e.target.value.toUpperCase())}
              placeholder="e.g., AAPL"
              className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2.5 text-sm text-gray-900 placeholder-gray-400 focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500 dark:border-gray-700 dark:bg-gray-900 dark:text-gray-100 dark:placeholder-gray-500"
              maxLength={20}
            />
          </div>

          {/* Alert Type */}
          <div>
            <label htmlFor="alert-type" className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
              Alert Type
            </label>
            <select
              id="alert-type"
              value={alertType}
              onChange={(e) => setAlertType(e.target.value as AlertType)}
              className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2.5 text-sm text-gray-900 focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500 dark:border-gray-700 dark:bg-gray-900 dark:text-gray-100"
            >
              <option value="NEWS_VOLUME">News Volume</option>
              <option value="SENTIMENT_CHANGE">Sentiment Change</option>
              <option value="BREAKING_NEWS">Breaking News</option>
            </select>
          </div>

          {/* Threshold Value (NEWS_VOLUME only) */}
          {alertType === 'NEWS_VOLUME' && (
            <div>
              <label htmlFor="alert-threshold" className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
                Threshold (number of articles)
              </label>
              <input
                id="alert-threshold"
                type="number"
                value={thresholdValue}
                onChange={(e) => setThresholdValue(e.target.value)}
                placeholder="e.g., 10"
                min={1}
                className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2.5 text-sm text-gray-900 placeholder-gray-400 focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500 dark:border-gray-700 dark:bg-gray-900 dark:text-gray-100 dark:placeholder-gray-500"
              />
            </div>
          )}

          {/* Target Sentiment (SENTIMENT_CHANGE only) */}
          {alertType === 'SENTIMENT_CHANGE' && (
            <div>
              <label htmlFor="alert-sentiment" className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
                Target Sentiment
              </label>
              <select
                id="alert-sentiment"
                value={targetSentiment}
                onChange={(e) => setTargetSentiment(e.target.value as AlertSentiment)}
                className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2.5 text-sm text-gray-900 focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500 dark:border-gray-700 dark:bg-gray-900 dark:text-gray-100"
              >
                <option value="POSITIVE">Positive</option>
                <option value="NEGATIVE">Negative</option>
                <option value="NEUTRAL">Neutral</option>
              </select>
            </div>
          )}

          {/* Frequency */}
          <div>
            <label htmlFor="alert-frequency" className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
              Check Frequency
            </label>
            <select
              id="alert-frequency"
              value={frequency}
              onChange={(e) => setFrequency(e.target.value as AlertFrequency)}
              className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2.5 text-sm text-gray-900 focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500 dark:border-gray-700 dark:bg-gray-900 dark:text-gray-100"
            >
              <option value="HOURLY">Hourly</option>
              <option value="DAILY">Daily</option>
              <option value="WEEKLY">Weekly</option>
            </select>
          </div>

          {/* Submit */}
          <div className="flex justify-end gap-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="rounded-lg border border-gray-300 px-4 py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-gray-600 dark:text-gray-300 dark:hover:bg-gray-700"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="flex items-center gap-2 rounded-lg bg-primary-600 px-4 py-2.5 text-sm font-medium text-white transition-colors hover:bg-primary-700 disabled:cursor-not-allowed disabled:opacity-50"
            >
              {isSubmitting ? (
                <Loader2 className="h-4 w-4 animate-spin" />
              ) : (
                <Plus className="h-4 w-4" />
              )}
              Create
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
