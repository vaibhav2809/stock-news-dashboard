import { useState } from 'react';
import {
  BarChart3,
  Search,
  Loader2,
  AlertCircle,
  TrendingUp,
  TrendingDown,
  Minus,
  RefreshCw,
} from 'lucide-react';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  LineChart,
  Line,
  Legend,
} from 'recharts';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useSentimentDistribution, useSentimentTimeline } from '@/hooks/useSentiment';
import { triggerNewsFetch } from '@/api/newsApi';
import { cn } from '@/utils';

/** Color constants for sentiment categories. */
const SENTIMENT_COLORS = {
  positive: '#22c55e',
  negative: '#ef4444',
  neutral: '#6b7280',
};

/** Number of days options for the timeline chart. */
const TIMELINE_DAY_OPTIONS = [7, 14, 30, 60, 90] as const;

/**
 * Sentiment analysis page showing distribution charts and timeline trends
 * for a user-selected stock symbol. Auto-fetches news articles from APIs before
 * computing sentiment, ensuring data is available even on first search.
 * Features pie chart, bar chart, and line chart.
 */
export function SentimentPage() {
  const [symbolInput, setSymbolInput] = useState('');
  const [activeSymbol, setActiveSymbol] = useState('');
  const [timelineDays, setTimelineDays] = useState<number>(30);
  const [isFetchingNews, setIsFetchingNews] = useState(false);
  const queryClient = useQueryClient();

  const {
    data: distribution,
    isLoading: isDistLoading,
    isError: isDistError,
    error: distError,
    refetch: refetchDistribution,
  } = useSentimentDistribution(activeSymbol);

  const {
    data: timeline,
    isLoading: isTimelineLoading,
    isError: isTimelineError,
    refetch: refetchTimeline,
  } = useSentimentTimeline(activeSymbol, timelineDays);

  /** Mutation to trigger news fetch for a symbol before querying sentiment. */
  const fetchNewsMutation = useMutation({
    mutationFn: (symbols: string[]) => triggerNewsFetch(symbols),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['sentiment'] });
    },
  });

  /**
   * Handles search submission.
   * First triggers a news fetch to ensure articles exist in the database,
   * then sets the active symbol to trigger sentiment queries.
   */
  const handleSearch = async () => {
    const trimmed = symbolInput.trim().toUpperCase();
    if (!trimmed) return;

    setIsFetchingNews(true);
    setActiveSymbol(''); // Reset to show loading state
    try {
      await fetchNewsMutation.mutateAsync([trimmed]);
    } catch {
      // Even if fetch fails (e.g., API key issue), still show whatever data exists
    }
    setIsFetchingNews(false);
    setActiveSymbol(trimmed);
  };

  /** Re-fetches news from APIs and refreshes sentiment data for the current symbol. */
  const handleRefresh = async () => {
    if (!activeSymbol) return;
    setIsFetchingNews(true);
    try {
      await fetchNewsMutation.mutateAsync([activeSymbol]);
    } catch {
      // Continue to refresh existing data even if API fetch fails
    }
    setIsFetchingNews(false);
    refetchDistribution();
    refetchTimeline();
  };

  /** Handles Enter key in the search input. */
  const handleKeyDown = (event: React.KeyboardEvent) => {
    if (event.key === 'Enter') {
      handleSearch();
    }
  };

  /** Pie chart data derived from the distribution. */
  const pieData = distribution
    ? [
        { name: 'Positive', value: distribution.positiveCount, color: SENTIMENT_COLORS.positive },
        { name: 'Negative', value: distribution.negativeCount, color: SENTIMENT_COLORS.negative },
        { name: 'Neutral', value: distribution.neutralCount, color: SENTIMENT_COLORS.neutral },
      ]
    : [];

  return (
    <div>
      {/* Header */}
      <div className="mb-8 flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
            Sentiment Analysis
          </h1>
          <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
            Analyze news sentiment trends and distributions for any stock symbol
          </p>
        </div>
        {activeSymbol && (
          <button
            onClick={handleRefresh}
            disabled={isFetchingNews}
            className="flex w-full items-center justify-center gap-2 rounded-lg border border-gray-300 px-3 py-2 text-sm font-medium text-gray-700 transition-colors hover:bg-gray-50 disabled:opacity-50 sm:w-auto dark:border-gray-600 dark:text-gray-300 dark:hover:bg-gray-800"
            title="Re-fetch news and refresh sentiment data"
          >
            <RefreshCw className={cn('h-4 w-4', isFetchingNews && 'animate-spin')} />
            Refresh
          </button>
        )}
      </div>

      {/* Search Bar */}
      <div className="mb-6 flex gap-3">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            value={symbolInput}
            onChange={(e) => setSymbolInput(e.target.value.toUpperCase())}
            onKeyDown={handleKeyDown}
            placeholder="Enter stock symbol (e.g., AAPL)"
            className="w-full rounded-lg border border-gray-300 bg-white py-2.5 pl-10 pr-4 text-sm text-gray-900 placeholder-gray-400 focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100 dark:placeholder-gray-500"
            maxLength={20}
            disabled={isFetchingNews}
          />
        </div>
        <button
          onClick={handleSearch}
          disabled={!symbolInput.trim() || isFetchingNews}
          className="flex items-center gap-2 rounded-lg bg-primary-600 px-5 py-2.5 text-sm font-medium text-white transition-colors hover:bg-primary-700 disabled:cursor-not-allowed disabled:opacity-50"
        >
          {isFetchingNews ? (
            <>
              <Loader2 className="h-4 w-4 animate-spin" />
              Fetching...
            </>
          ) : (
            'Analyze'
          )}
        </button>
      </div>

      {/* Fetching News Indicator */}
      {isFetchingNews && (
        <div className="mb-6 flex items-center gap-3 rounded-lg border border-primary-200 bg-primary-50 p-4 dark:border-primary-800 dark:bg-primary-900/20">
          <Loader2 className="h-5 w-5 animate-spin text-primary-600 dark:text-primary-400" />
          <div>
            <p className="text-sm font-medium text-primary-700 dark:text-primary-300">
              Fetching latest news articles...
            </p>
            <p className="text-xs text-primary-600 dark:text-primary-400">
              Pulling articles from Finnhub and NewsData.io for sentiment analysis
            </p>
          </div>
        </div>
      )}

      {/* Empty State - Before Search */}
      {!activeSymbol && !isFetchingNews && (
        <div className="flex flex-col items-center justify-center rounded-xl border-2 border-dashed border-gray-300 py-16 dark:border-gray-700">
          <BarChart3 className="mb-4 h-12 w-12 text-gray-400 dark:text-gray-600" />
          <h2 className="text-lg font-semibold text-gray-600 dark:text-gray-400">
            Search for a symbol to analyze
          </h2>
          <p className="mt-2 max-w-md text-center text-sm text-gray-500">
            Enter a stock ticker above to fetch the latest news and see sentiment distribution, trends, and historical data.
          </p>
        </div>
      )}

      {/* Loading State */}
      {activeSymbol && (isDistLoading || isTimelineLoading) && !isFetchingNews && (
        <div className="flex items-center justify-center py-16">
          <Loader2 className="h-8 w-8 animate-spin text-primary-500" />
          <span className="ml-3 text-gray-500 dark:text-gray-400">Analyzing {activeSymbol}...</span>
        </div>
      )}

      {/* Error State */}
      {isDistError && !isFetchingNews && (
        <div className="flex flex-col items-center justify-center py-16">
          <AlertCircle className="mb-3 h-10 w-10 text-red-500" />
          <p className="text-sm text-red-600 dark:text-red-400">
            {(distError as Error)?.message || 'Failed to load sentiment data'}
          </p>
        </div>
      )}

      {/* Results */}
      {distribution && !isDistLoading && !isFetchingNews && (
        <div className="space-y-6">
          {/* Summary Cards */}
          <div className="grid gap-4 grid-cols-2 lg:grid-cols-4">
            {/* Total Articles */}
            <div className="rounded-xl border border-gray-200 bg-white p-4 dark:border-gray-700 dark:bg-gray-800">
              <p className="text-xs font-medium uppercase tracking-wider text-gray-500 dark:text-gray-400">
                Total Articles
              </p>
              <p className="mt-1 text-2xl font-bold text-gray-900 dark:text-gray-100">
                {distribution.totalArticles}
              </p>
            </div>

            {/* Positive */}
            <div className="rounded-xl border border-green-200 bg-green-50 p-4 dark:border-green-800 dark:bg-green-900/20">
              <p className="flex items-center gap-1.5 text-xs font-medium uppercase tracking-wider text-green-700 dark:text-green-400">
                <TrendingUp className="h-3.5 w-3.5" />
                Positive
              </p>
              <p className="mt-1 text-2xl font-bold text-green-700 dark:text-green-400">
                {distribution.positivePercent}%
              </p>
              <p className="text-xs text-green-600 dark:text-green-500">
                {distribution.positiveCount} articles
              </p>
            </div>

            {/* Negative */}
            <div className="rounded-xl border border-red-200 bg-red-50 p-4 dark:border-red-800 dark:bg-red-900/20">
              <p className="flex items-center gap-1.5 text-xs font-medium uppercase tracking-wider text-red-700 dark:text-red-400">
                <TrendingDown className="h-3.5 w-3.5" />
                Negative
              </p>
              <p className="mt-1 text-2xl font-bold text-red-700 dark:text-red-400">
                {distribution.negativePercent}%
              </p>
              <p className="text-xs text-red-600 dark:text-red-500">
                {distribution.negativeCount} articles
              </p>
            </div>

            {/* Neutral */}
            <div className="rounded-xl border border-gray-200 bg-gray-50 p-4 dark:border-gray-700 dark:bg-gray-800">
              <p className="flex items-center gap-1.5 text-xs font-medium uppercase tracking-wider text-gray-600 dark:text-gray-400">
                <Minus className="h-3.5 w-3.5" />
                Neutral
              </p>
              <p className="mt-1 text-2xl font-bold text-gray-700 dark:text-gray-300">
                {distribution.neutralPercent}%
              </p>
              <p className="text-xs text-gray-500 dark:text-gray-400">
                {distribution.neutralCount} articles
              </p>
            </div>
          </div>

          {/* Charts Row */}
          <div className="grid gap-6 lg:grid-cols-2">
            {/* Pie Chart */}
            <div className="rounded-xl border border-gray-200 bg-white p-5 dark:border-gray-700 dark:bg-gray-800">
              <h3 className="mb-4 text-sm font-semibold text-gray-900 dark:text-gray-100">
                Sentiment Distribution — {activeSymbol}
              </h3>
              {distribution.totalArticles > 0 ? (
                <ResponsiveContainer width="100%" height={280}>
                  <PieChart>
                    <Pie
                      data={pieData}
                      cx="50%"
                      cy="50%"
                      innerRadius={60}
                      outerRadius={100}
                      dataKey="value"
                      label={({ name, percent }: { name?: string; percent?: number }) =>
                        `${name ?? ''} ${((percent ?? 0) * 100).toFixed(0)}%`
                      }
                    >
                      {pieData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={entry.color} />
                      ))}
                    </Pie>
                    <Tooltip />
                  </PieChart>
                </ResponsiveContainer>
              ) : (
                <div className="flex h-[280px] items-center justify-center text-sm text-gray-500">
                  No articles found for {activeSymbol}
                </div>
              )}
            </div>

            {/* Average Score Gauge */}
            <div className="rounded-xl border border-gray-200 bg-white p-5 dark:border-gray-700 dark:bg-gray-800">
              <h3 className="mb-4 text-sm font-semibold text-gray-900 dark:text-gray-100">
                Average Sentiment Score
              </h3>
              <div className="flex h-[280px] flex-col items-center justify-center">
                <div
                  className={cn(
                    'text-6xl font-bold',
                    distribution.averageScore > 0.1
                      ? 'text-green-500'
                      : distribution.averageScore < -0.1
                        ? 'text-red-500'
                        : 'text-gray-500',
                  )}
                >
                  {distribution.averageScore > 0 ? '+' : ''}
                  {distribution.averageScore.toFixed(3)}
                </div>
                <p className="mt-2 text-sm text-gray-500 dark:text-gray-400">
                  Scale: -1.0 (very negative) to +1.0 (very positive)
                </p>
                <div className="mt-4 flex items-center gap-2">
                  {distribution.averageScore > 0.1 ? (
                    <span className="flex items-center gap-1 rounded-full bg-green-100 px-3 py-1 text-sm font-medium text-green-700 dark:bg-green-900/30 dark:text-green-400">
                      <TrendingUp className="h-4 w-4" /> Bullish
                    </span>
                  ) : distribution.averageScore < -0.1 ? (
                    <span className="flex items-center gap-1 rounded-full bg-red-100 px-3 py-1 text-sm font-medium text-red-700 dark:bg-red-900/30 dark:text-red-400">
                      <TrendingDown className="h-4 w-4" /> Bearish
                    </span>
                  ) : (
                    <span className="flex items-center gap-1 rounded-full bg-gray-100 px-3 py-1 text-sm font-medium text-gray-700 dark:bg-gray-700 dark:text-gray-300">
                      <Minus className="h-4 w-4" /> Neutral
                    </span>
                  )}
                </div>
              </div>
            </div>
          </div>

          {/* Timeline Chart */}
          {timeline && (
            <div className="rounded-xl border border-gray-200 bg-white p-5 dark:border-gray-700 dark:bg-gray-800">
              <div className="mb-4 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                <h3 className="text-sm font-semibold text-gray-900 dark:text-gray-100">
                  Sentiment Timeline — {activeSymbol}
                </h3>
                <div className="flex flex-wrap gap-1">
                  {TIMELINE_DAY_OPTIONS.map((days) => (
                    <button
                      key={days}
                      onClick={() => setTimelineDays(days)}
                      className={cn(
                        'rounded-md px-2.5 py-1 text-xs font-medium transition-colors',
                        timelineDays === days
                          ? 'bg-primary-100 text-primary-700 dark:bg-primary-900/30 dark:text-primary-400'
                          : 'text-gray-500 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-gray-700',
                      )}
                    >
                      {days}d
                    </button>
                  ))}
                </div>
              </div>

              {isTimelineLoading ? (
                <div className="flex h-[300px] items-center justify-center">
                  <Loader2 className="h-6 w-6 animate-spin text-primary-500" />
                </div>
              ) : isTimelineError ? (
                <div className="flex h-[300px] items-center justify-center text-sm text-red-500">
                  Failed to load timeline data
                </div>
              ) : timeline.length > 0 ? (
                <div className="space-y-6">
                  {/* Stacked Bar Chart - Article counts by sentiment */}
                  <ResponsiveContainer width="100%" height={250}>
                    <BarChart data={timeline}>
                      <CartesianGrid strokeDasharray="3 3" className="opacity-30" />
                      <XAxis
                        dataKey="date"
                        tickFormatter={(value: string) => {
                          const date = new Date(value);
                          return `${date.getMonth() + 1}/${date.getDate()}`;
                        }}
                        tick={{ fontSize: 11, fill: '#9ca3af' }}
                      />
                      <YAxis tick={{ fontSize: 11, fill: '#9ca3af' }} />
                      <Tooltip
                        labelFormatter={(value) => new Date(String(value)).toLocaleDateString()}
                        contentStyle={{
                          backgroundColor: 'rgba(17, 24, 39, 0.9)',
                          border: 'none',
                          borderRadius: '8px',
                          color: '#fff',
                          fontSize: '12px',
                        }}
                      />
                      <Legend />
                      <Bar dataKey="positiveCount" name="Positive" fill={SENTIMENT_COLORS.positive} stackId="a" />
                      <Bar dataKey="neutralCount" name="Neutral" fill={SENTIMENT_COLORS.neutral} stackId="a" />
                      <Bar dataKey="negativeCount" name="Negative" fill={SENTIMENT_COLORS.negative} stackId="a" />
                    </BarChart>
                  </ResponsiveContainer>

                  {/* Line Chart - Average score over time */}
                  <ResponsiveContainer width="100%" height={200}>
                    <LineChart data={timeline}>
                      <CartesianGrid strokeDasharray="3 3" className="opacity-30" />
                      <XAxis
                        dataKey="date"
                        tickFormatter={(value: string) => {
                          const date = new Date(value);
                          return `${date.getMonth() + 1}/${date.getDate()}`;
                        }}
                        tick={{ fontSize: 11, fill: '#9ca3af' }}
                      />
                      <YAxis
                        domain={[-1, 1]}
                        tick={{ fontSize: 11, fill: '#9ca3af' }}
                        tickFormatter={(value: number) => value.toFixed(1)}
                      />
                      <Tooltip
                        labelFormatter={(value) => new Date(String(value)).toLocaleDateString()}
                        formatter={(value) => [Number(value).toFixed(3), 'Avg Score']}
                        contentStyle={{
                          backgroundColor: 'rgba(17, 24, 39, 0.9)',
                          border: 'none',
                          borderRadius: '8px',
                          color: '#fff',
                          fontSize: '12px',
                        }}
                      />
                      <Line
                        type="monotone"
                        dataKey="averageScore"
                        name="Avg Sentiment"
                        stroke="#6366f1"
                        strokeWidth={2}
                        dot={false}
                      />
                    </LineChart>
                  </ResponsiveContainer>
                </div>
              ) : (
                <div className="flex h-[300px] items-center justify-center text-sm text-gray-500">
                  No timeline data available for this period
                </div>
              )}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
