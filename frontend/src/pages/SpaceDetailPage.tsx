import { useState, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  ArrowLeft,
  ExternalLink,
  Clock,
  Trash2,
  Folder,
} from 'lucide-react';
import { useSpace, useSpaceArticles, useRemoveArticle, useDeleteSpace } from '@/hooks';
import { LoadingSpinner, EmptyState, ErrorBanner } from '@/components/ui';
import { SentimentBadge, SourceBadge } from '@/components/ui';
import { formatRelativeTime } from '@/utils';
import type { SavedArticle } from '@/types';
import type { NewsSource, Sentiment } from '@/types';

/**
 * Space detail page showing all saved articles within a specific Space.
 * Allows removing individual articles and deleting the entire space.
 */
export function SpaceDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const spaceId = Number(id);

  const { data: space, isLoading: isLoadingSpace, error: spaceError } = useSpace(spaceId);
  const { data: articles = [], isLoading: isLoadingArticles, error: articlesError, refetch } = useSpaceArticles(spaceId);
  const removeArticleMutation = useRemoveArticle();
  const deleteSpaceMutation = useDeleteSpace();

  const [confirmDeleteId, setConfirmDeleteId] = useState<number | null>(null);

  /** Removes a saved article after confirmation. */
  const handleRemoveArticle = useCallback(
    (articleId: number) => {
      removeArticleMutation.mutate(
        { spaceId, articleId },
        { onSuccess: () => setConfirmDeleteId(null) },
      );
    },
    [spaceId, removeArticleMutation],
  );

  /** Deletes the entire space and navigates back. */
  const handleDeleteSpace = useCallback(() => {
    deleteSpaceMutation.mutate(spaceId, {
      onSuccess: () => navigate('/spaces'),
    });
  }, [spaceId, deleteSpaceMutation, navigate]);

  if (isLoadingSpace || isLoadingArticles) {
    return <LoadingSpinner size="large" message="Loading space..." />;
  }

  if (spaceError || articlesError) {
    return (
      <ErrorBanner
        message={(spaceError as Error)?.message || (articlesError as Error)?.message || 'Failed to load space'}
        onRetry={() => refetch()}
      />
    );
  }

  if (!space) {
    return <ErrorBanner message="Space not found" />;
  }

  return (
    <div className="space-y-6">
      {/* Back navigation + Header */}
      <div>
        <Link
          to="/spaces"
          className="mb-4 inline-flex items-center gap-1.5 text-sm text-gray-500 transition-colors hover:text-gray-900 dark:text-gray-400 dark:hover:text-gray-100"
        >
          <ArrowLeft className="h-4 w-4" />
          Back to Spaces
        </Link>

        <div className="flex items-start justify-between">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary-100 dark:bg-primary-900/30">
              <Folder className="h-5 w-5 text-primary-600 dark:text-primary-400" />
            </div>
            <div>
              <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
                {space.name}
              </h1>
              {space.description && (
                <p className="mt-0.5 text-sm text-gray-500 dark:text-gray-400">
                  {space.description}
                </p>
              )}
              <p className="mt-1 text-xs text-gray-400 dark:text-gray-500">
                {articles.length} {articles.length === 1 ? 'article' : 'articles'} · Created{' '}
                {formatRelativeTime(space.createdAt)}
              </p>
            </div>
          </div>

          <button
            onClick={handleDeleteSpace}
            disabled={deleteSpaceMutation.isPending}
            className="flex items-center gap-1.5 rounded-lg border border-red-200 px-3 py-2 text-sm font-medium text-red-600 transition-colors hover:bg-red-50 disabled:opacity-50 dark:border-red-900/50 dark:text-red-400 dark:hover:bg-red-950/30"
          >
            <Trash2 className="h-4 w-4" />
            {deleteSpaceMutation.isPending ? 'Deleting...' : 'Delete Space'}
          </button>
        </div>
      </div>

      {/* Articles list */}
      {articles.length === 0 ? (
        <EmptyState
          icon={Folder}
          title="No saved articles"
          description="Save articles from the News Feed by clicking the bookmark icon on any article card."
          action={
            <Link
              to="/"
              className="inline-flex items-center gap-2 rounded-lg bg-primary-600 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-primary-700 dark:bg-primary-500 dark:hover:bg-primary-600"
            >
              Go to News Feed
            </Link>
          }
        />
      ) : (
        <div className="space-y-3">
          {articles.map((article) => (
            <SavedArticleRow
              key={article.id}
              article={article}
              isConfirmingDelete={confirmDeleteId === article.id}
              isDeleting={removeArticleMutation.isPending && confirmDeleteId === article.id}
              onConfirmDelete={() => setConfirmDeleteId(article.id)}
              onCancelDelete={() => setConfirmDeleteId(null)}
              onDelete={() => handleRemoveArticle(article.id)}
            />
          ))}
        </div>
      )}
    </div>
  );
}

interface SavedArticleRowProps {
  article: SavedArticle;
  isConfirmingDelete: boolean;
  isDeleting: boolean;
  onConfirmDelete: () => void;
  onCancelDelete: () => void;
  onDelete: () => void;
}

/**
 * A single saved article row in the space detail page.
 * Shows article info in a horizontal layout with a remove button.
 */
function SavedArticleRow({
  article,
  isConfirmingDelete,
  isDeleting,
  onConfirmDelete,
  onCancelDelete,
  onDelete,
}: SavedArticleRowProps) {
  return (
    <div className="flex items-start gap-4 rounded-lg border border-gray-200 bg-white p-4 dark:border-gray-800 dark:bg-gray-900">
      {/* Thumbnail */}
      {article.imageUrl && (
        <a
          href={article.sourceUrl}
          target="_blank"
          rel="noopener noreferrer"
          className="hidden h-20 w-28 flex-shrink-0 overflow-hidden rounded-md sm:block"
        >
          <img
            src={article.imageUrl}
            alt={article.title}
            className="h-full w-full object-cover"
            loading="lazy"
          />
        </a>
      )}

      {/* Content */}
      <div className="flex-1">
        <div className="mb-1.5 flex flex-wrap items-center gap-2">
          {article.symbol && (
            <span className="inline-flex items-center rounded-md bg-primary-100 px-2 py-0.5 text-xs font-semibold text-primary-800 dark:bg-primary-900/30 dark:text-primary-400">
              {article.symbol}
            </span>
          )}
          <SourceBadge source={article.source as NewsSource} />
          {article.sentiment && (
            <SentimentBadge sentiment={article.sentiment as Sentiment} />
          )}
        </div>

        <a
          href={article.sourceUrl}
          target="_blank"
          rel="noopener noreferrer"
          className="mb-1 block text-sm font-semibold text-gray-900 transition-colors hover:text-primary-600 dark:text-gray-100 dark:hover:text-primary-400"
        >
          {article.title}
        </a>

        {article.summary && (
          <p className="mb-2 line-clamp-2 text-xs text-gray-500 dark:text-gray-400">
            {article.summary}
          </p>
        )}

        <div className="flex items-center gap-3 text-xs text-gray-400 dark:text-gray-500">
          {article.publishedAt && (
            <span className="flex items-center gap-1">
              <Clock className="h-3 w-3" />
              {formatRelativeTime(article.publishedAt)}
            </span>
          )}
          <a
            href={article.sourceUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-1 transition-colors hover:text-primary-600 dark:hover:text-primary-400"
          >
            <ExternalLink className="h-3 w-3" />
            Read
          </a>
        </div>
      </div>

      {/* Delete button */}
      <div className="flex-shrink-0">
        {isConfirmingDelete ? (
          <div className="flex items-center gap-1">
            <button
              onClick={onDelete}
              disabled={isDeleting}
              className="rounded-md bg-red-600 px-2 py-1 text-xs font-medium text-white hover:bg-red-700 disabled:opacity-50"
            >
              {isDeleting ? '...' : 'Remove'}
            </button>
            <button
              onClick={onCancelDelete}
              className="rounded-md px-2 py-1 text-xs text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-800"
            >
              Cancel
            </button>
          </div>
        ) : (
          <button
            onClick={onConfirmDelete}
            className="rounded-md p-1.5 text-gray-400 transition-colors hover:bg-red-50 hover:text-red-600 dark:hover:bg-red-900/20 dark:hover:text-red-400"
            title="Remove from space"
          >
            <Trash2 className="h-4 w-4" />
          </button>
        )}
      </div>
    </div>
  );
}
