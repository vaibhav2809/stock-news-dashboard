import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Loader2, AlertCircle } from 'lucide-react';
import { login } from '@/api/authApi';
import { useAuthStore } from '@/store/authStore';

/**
 * Login page with email/password form.
 * On success, stores JWT tokens and redirects to the original page or dashboard.
 */
export function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const setAuth = useAuthStore((state) => state.setAuth);
  const navigate = useNavigate();
  const location = useLocation();

  /** The page the user was trying to access before being redirected to login. */
  const redirectTo = (location.state as { from?: string })?.from || '/';

  /** Handles form submission. */
  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setErrorMessage('');
    setIsSubmitting(true);

    try {
      const response = await login({ email: email.trim(), password });
      setAuth(response);
      navigate(redirectTo, { replace: true });
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Login failed';
      setErrorMessage(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  /** Whether the submit button should be enabled. */
  const isFormValid = email.trim().length > 0 && password.length > 0;

  return (
    <div>
      <h2 className="mb-1 text-xl font-semibold text-white">Welcome back</h2>
      <p className="mb-6 text-sm text-gray-400">Sign in to your account</p>

      {/* Error Banner */}
      {errorMessage && (
        <div className="mb-4 flex items-center gap-2 rounded-lg border border-red-800 bg-red-900/20 p-3 text-sm text-red-400">
          <AlertCircle className="h-4 w-4 shrink-0" />
          {errorMessage}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Email */}
        <div>
          <label htmlFor="email" className="mb-1.5 block text-sm font-medium text-gray-300">
            Email
          </label>
          <input
            id="email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="you@example.com"
            required
            autoComplete="email"
            className="w-full rounded-lg border border-gray-700 bg-gray-800 px-3.5 py-2.5 text-sm text-white placeholder-gray-500 focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500"
          />
        </div>

        {/* Password */}
        <div>
          <label htmlFor="password" className="mb-1.5 block text-sm font-medium text-gray-300">
            Password
          </label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="Enter your password"
            required
            autoComplete="current-password"
            className="w-full rounded-lg border border-gray-700 bg-gray-800 px-3.5 py-2.5 text-sm text-white placeholder-gray-500 focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500"
          />
        </div>

        {/* Submit */}
        <button
          type="submit"
          disabled={!isFormValid || isSubmitting}
          className="flex w-full items-center justify-center gap-2 rounded-lg bg-primary-600 px-4 py-2.5 text-sm font-medium text-white transition-colors hover:bg-primary-700 disabled:cursor-not-allowed disabled:opacity-50"
        >
          {isSubmitting && <Loader2 className="h-4 w-4 animate-spin" />}
          Sign In
        </button>
      </form>

      {/* Register Link */}
      <p className="mt-6 text-center text-sm text-gray-400">
        Don&apos;t have an account?{' '}
        <Link to="/register" className="font-medium text-primary-400 hover:text-primary-300">
          Create one
        </Link>
      </p>
    </div>
  );
}
