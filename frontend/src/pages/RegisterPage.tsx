import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Loader2, AlertCircle } from 'lucide-react';
import { register } from '@/api/authApi';
import { useAuthStore } from '@/store/authStore';

/**
 * Registration page with email, password, and display name form.
 * On success, stores JWT tokens and redirects to the dashboard.
 */
export function RegisterPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const setAuth = useAuthStore((state) => state.setAuth);
  const navigate = useNavigate();

  /** Handles form submission. */
  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setErrorMessage('');

    if (password.length < 8) {
      setErrorMessage('Password must be at least 8 characters');
      return;
    }

    setIsSubmitting(true);

    try {
      const response = await register({
        email: email.trim(),
        password,
        displayName: displayName.trim(),
      });
      setAuth(response);
      navigate('/', { replace: true });
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Registration failed';
      setErrorMessage(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  /** Whether the submit button should be enabled. */
  const isFormValid =
    email.trim().length > 0 && password.length >= 8 && displayName.trim().length > 0;

  return (
    <div>
      <h2 className="mb-1 text-xl font-semibold text-white">Create an account</h2>
      <p className="mb-6 text-sm text-gray-400">Start tracking stock news and sentiment</p>

      {/* Error Banner */}
      {errorMessage && (
        <div className="mb-4 flex items-center gap-2 rounded-lg border border-red-800 bg-red-900/20 p-3 text-sm text-red-400">
          <AlertCircle className="h-4 w-4 shrink-0" />
          {errorMessage}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Display Name */}
        <div>
          <label htmlFor="displayName" className="mb-1.5 block text-sm font-medium text-gray-300">
            Display Name
          </label>
          <input
            id="displayName"
            type="text"
            value={displayName}
            onChange={(e) => setDisplayName(e.target.value)}
            placeholder="John Doe"
            required
            maxLength={100}
            autoComplete="name"
            className="w-full rounded-lg border border-gray-700 bg-gray-800 px-3.5 py-2.5 text-sm text-white placeholder-gray-500 focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500"
          />
        </div>

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
            placeholder="At least 8 characters"
            required
            minLength={8}
            maxLength={100}
            autoComplete="new-password"
            className="w-full rounded-lg border border-gray-700 bg-gray-800 px-3.5 py-2.5 text-sm text-white placeholder-gray-500 focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500"
          />
          <p className="mt-1 text-xs text-gray-500">Minimum 8 characters</p>
        </div>

        {/* Submit */}
        <button
          type="submit"
          disabled={!isFormValid || isSubmitting}
          className="flex w-full items-center justify-center gap-2 rounded-lg bg-primary-600 px-4 py-2.5 text-sm font-medium text-white transition-colors hover:bg-primary-700 disabled:cursor-not-allowed disabled:opacity-50"
        >
          {isSubmitting && <Loader2 className="h-4 w-4 animate-spin" />}
          Create Account
        </button>
      </form>

      {/* Login Link */}
      <p className="mt-6 text-center text-sm text-gray-400">
        Already have an account?{' '}
        <Link to="/login" className="font-medium text-primary-400 hover:text-primary-300">
          Sign in
        </Link>
      </p>
    </div>
  );
}
