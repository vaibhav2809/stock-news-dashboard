import { useState } from 'react';
import { ExternalLink, Clock, Tag, Bookmark } from 'lucide-react';
import type { NewsArticle } from '@/types';
import { SentimentBadge, SourceBadge } from '@/components/ui';
import { SaveToSpaceModal } from '@/components/spaces/SaveToSpaceModal';
import { formatRelativeTime } from '@/utils';

interface NewsCardProps {
  /** The news article data to display */
  article: NewsArticle;
}

/** Fallback gradient shown when an article has no image */
const PLACEHOLDER_GRADIENT =
  'bg-gradient-to-br from-gray-200 to-gray-300 dark:from-gray-700 dark:to-gray-800';

/**
 * Card component displaying a single news article.
 * Shows thumbnail, title, summary, sentiment badge, source badge,
 * symbol tag, relative publish time, and a save button.
 *
 * @param article - The news article to render
 */
export function NewsCard({ article }: NewsCardProps) {
  const [isSaveModalOpen, setIsSaveModalOpen] = useState(false);

  return (
    <>
      <article className="group flex flex-col overflow-hidden rounded-xl border border-gray-200 bg-white transition-shadow hover:shadow-lg dark:border-gray-800 dark:bg-gray-900">
        {/* Thumbnail */}
        <a
          href={article.sourceUrl}
          target="_blank"
          rel="noopener noreferrer"
          className="relative block h-44 w-full overflow-hidden"
        >
          {article.imageUrl ? (
            <img
              src={article.imageUrl}
              alt={article.title}
              className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
              loading="lazy"
            />
          ) : (
            <div className={`flex h-full w-full items-center justify-center ${PLACEHOLDER_GRADIENT}`}>
              <Tag className="h-10 w-10 text-gray-400 dark:text-gray-600" />
            </div>
          )}
          {/* Source badge overlay */}
          <div className="absolute left-2 top-2">
            <SourceBadge source={article.source} />
          </div>
          {/* Save button overlay */}
          <button
            onClick={(e) => {
              e.preventDefault();
              setIsSaveModalOpen(true);
            }}
            className="absolute right-2 top-2 rounded-full bg-white/80 p-1.5 text-gray-600 opacity-0 shadow-sm backdrop-blur-sm transition-all hover:bg-white hover:text-primary-600 group-hover:opacity-100 dark:bg-gray-900/80 dark:text-gray-400 dark:hover:bg-gray-900 dark:hover:text-primary-400"
            aria-label="Save article"
            title="Save to Space"
          >
            <Bookmark className="h-4 w-4" />
          </button>
        </a>

        {/* Content */}
        <div className="flex flex-1 flex-col p-4">
          {/* Symbol + Sentiment row */}
          <div className="mb-2 flex items-center gap-2">
            <span className="inline-flex items-center rounded-md bg-primary-100 px-2 py-0.5 text-xs font-semibold text-primary-800 dark:bg-primary-900/30 dark:text-primary-400">
              {article.symbol}
            </span>
            <SentimentBadge sentiment={article.sentiment} />
          </div>

          {/* Title */}
          <a
            href={article.sourceUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="mb-2 line-clamp-2 text-sm font-semibold text-gray-900 transition-colors hover:text-primary-600 dark:text-gray-100 dark:hover:text-primary-400"
          >
            {article.title}
          </a>

          {/* Summary */}
          {article.summary && (
            <p className="mb-3 line-clamp-3 flex-1 text-xs text-gray-600 dark:text-gray-400">
              {article.summary}
            </p>
          )}

          {/* Footer: time + save + external link */}
          <div className="mt-auto flex items-center justify-between border-t border-gray-100 pt-3 dark:border-gray-800">
            <div className="flex items-center gap-1 text-xs text-gray-500 dark:text-gray-500">
              <Clock className="h-3 w-3" />
              <time dateTime={article.publishedAt}>
                {formatRelativeTime(article.publishedAt)}
              </time>
            </div>
            <div className="flex items-center gap-3">
              <button
                onClick={() => setIsSaveModalOpen(true)}
                className="flex items-center gap-1 text-xs text-gray-500 transition-colors hover:text-primary-600 dark:text-gray-500 dark:hover:text-primary-400"
                aria-label="Save to space"
              >
                <Bookmark className="h-3 w-3" />
                Save
              </button>
              <a
                href={article.sourceUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="flex items-center gap-1 text-xs text-gray-500 transition-colors hover:text-primary-600 dark:text-gray-500 dark:hover:text-primary-400"
                aria-label={`Read full article: ${article.title}`}
              >
                Read more
                <ExternalLink className="h-3 w-3" />
              </a>
            </div>
          </div>
        </div>
      </article>

      {/* Save modal */}
      <SaveToSpaceModal
        article={article}
        isOpen={isSaveModalOpen}
        onClose={() => setIsSaveModalOpen(false)}
      />
    </>
  );
}
