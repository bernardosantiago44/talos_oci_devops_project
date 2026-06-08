import { FormEvent, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { ArrowRight, Layers, LogIn } from 'lucide-react';
import { useAuth } from '@/contexts/auth-context';
import { getAuthErrorMessage } from '@/auth/auth-errors';

type LocationState = {
  from?: {
    pathname?: string;
  };
};

export function LoginPage() {
  const auth = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const redirectPath = (location.state as LocationState | null)?.from?.pathname ?? '/';

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    setError('');

    try {
      await auth.login({ email, password });
      navigate(redirectPath, { replace: true });
    } catch (requestError) {
      setError(getAuthErrorMessage(requestError, 'Unable to log in.'));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-zinc-50 px-4 py-10 text-zinc-900 dark:bg-zinc-950 dark:text-zinc-100">
      <div className="w-full max-w-md rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm dark:border-zinc-800 dark:bg-zinc-900/70">
        <div className="mb-6 flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-sky-500/15 text-sky-600 dark:text-sky-400">
            <Layers className="h-5 w-5" />
          </div>
          <div>
            <h1 className="text-xl font-bold text-zinc-900 dark:text-zinc-100">Talos OCI DevOps</h1>
            <p className="text-sm text-zinc-500 dark:text-zinc-400">Sign in to continue</p>
          </div>
        </div>

        {error && (
          <p className="mb-4 rounded-xl border border-rose-200 bg-rose-50 px-4 py-2.5 text-sm text-rose-600 dark:border-rose-500/20 dark:bg-rose-500/10 dark:text-rose-400">
            {error}
          </p>
        )}

        <form onSubmit={handleSubmit} className="grid gap-4">
          <label className="grid gap-1.5 text-sm font-medium text-zinc-700 dark:text-zinc-300">
            Email
            <input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              autoComplete="email"
              required
              className="rounded-xl border border-zinc-200 bg-zinc-50 px-3 py-2.5 text-sm text-zinc-900 outline-none focus:border-sky-400/60 focus:ring-1 focus:ring-sky-400/30 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-100"
            />
          </label>

          <label className="grid gap-1.5 text-sm font-medium text-zinc-700 dark:text-zinc-300">
            Password
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              autoComplete="current-password"
              required
              className="rounded-xl border border-zinc-200 bg-zinc-50 px-3 py-2.5 text-sm text-zinc-900 outline-none focus:border-sky-400/60 focus:ring-1 focus:ring-sky-400/30 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-100"
            />
          </label>

          <button
            type="submit"
            disabled={submitting}
            className="mt-2 inline-flex items-center justify-center gap-2 rounded-xl bg-sky-500 px-4 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-sky-600 disabled:cursor-not-allowed disabled:opacity-70 dark:hover:bg-sky-400"
          >
            <LogIn className="h-4 w-4" />
            {submitting ? 'Signing in...' : 'Sign in'}
          </button>
        </form>

        <div className="mt-5 flex items-center justify-between text-sm">
          <span className="text-zinc-500 dark:text-zinc-400">New account?</span>
          <Link to="/signup" className="inline-flex items-center gap-1.5 font-semibold text-sky-600 hover:text-sky-700 dark:text-sky-400 dark:hover:text-sky-300">
            Sign up
            <ArrowRight className="h-4 w-4" />
          </Link>
        </div>
      </div>
    </div>
  );
}
