/** Types of alerts users can create. */
export type AlertType = 'NEWS_VOLUME' | 'SENTIMENT_CHANGE' | 'BREAKING_NEWS';

/** How frequently an alert checks its condition. */
export type AlertFrequency = 'HOURLY' | 'DAILY' | 'WEEKLY';

/** Sentiment values for sentiment-change alerts. */
export type AlertSentiment = 'POSITIVE' | 'NEGATIVE' | 'NEUTRAL';

/** An alert configured by the user. */
export interface Alert {
  id: number;
  symbol: string;
  alertType: AlertType;
  thresholdValue: number | null;
  targetSentiment: AlertSentiment | null;
  frequency: AlertFrequency;
  isActive: boolean;
  lastTriggeredAt: string | null;
  createdAt: string;
}

/** Request body for creating a new alert. */
export interface CreateAlertRequest {
  symbol: string;
  alertType: AlertType;
  thresholdValue?: number;
  targetSentiment?: AlertSentiment;
  frequency: AlertFrequency;
}
