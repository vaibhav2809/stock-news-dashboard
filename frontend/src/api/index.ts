export { default as apiClient } from './apiClient';
export { fetchNewsArticles, fetchNewsArticleById, fetchTrendingNews, triggerNewsFetch } from './newsApi';
export {
  fetchSpaces, fetchSpaceById, createSpace, updateSpace, deleteSpace,
  fetchArticlesInSpace, saveArticleToSpace, removeArticleFromSpace,
} from './spacesApi';
export { searchSymbols } from './symbolsApi';
export type { StockSymbol } from './symbolsApi';
export { fetchAlerts, createAlert, toggleAlert, deleteAlert } from './alertsApi';
