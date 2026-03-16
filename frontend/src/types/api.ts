/**
 * Standard API error response shape from the backend.
 */
export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  fieldErrors?: Record<string, string>;
}

/**
 * Generic paginated response wrapper.
 * All list endpoints return data in this shape.
 */
export interface PaginatedResponse<T> {
  data: T[];
  page: number;
  totalPages: number;
  totalElements: number;
}

/**
 * Standard API success response wrapper.
 */
export interface ApiResponse<T> {
  data: T;
  status: number;
}
