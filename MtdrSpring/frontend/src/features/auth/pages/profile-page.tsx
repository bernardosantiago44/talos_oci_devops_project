import { FormEvent, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ArrowLeft, Save, UserCog } from 'lucide-react';
import { useAuth } from '@/contexts/auth-context';
import { getAuthErrorMessage } from '@/auth/auth-errors';

export function ProfilePage() {
  const auth = useAuth();
  const [name, setName] = useState('');
  const [telegramUserId, setTelegramUserId] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    setName(auth.user?.name ?? '');
    setTelegramUserId(auth.user?.telegramUserId ?? '');
    setPhoneNumber(auth.user?.phoneNumber ?? '');
    setPassword('');
  }, [auth.user]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    setError('');
    setSuccess('');

    try {
      await auth.updateProfile({
        name,
        telegramUserId,
        phoneNumber,
        ...(password.trim() ? { password } : {}),
      });
      setPassword('');
      setSuccess('Profile saved.');
    } catch (requestError) {
      setError(getAuthErrorMessage(requestError, 'Unable to save profile.'));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="min-h-screen bg-zinc-50 px-4 py-8 text-zinc-900 dark:bg-zinc-950 dark:text-zinc-100 sm:px-6">
      <div className="mx-auto max-w-2xl">
        <div className="mb-6 flex items-center justify-between gap-3">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-sky-500/15 text-sky-600 dark:text-sky-400">
              <UserCog className="h-5 w-5" />
            </div>
            <div>
              <h1 className="text-xl font-bold text-zinc-900 dark:text-zinc-100">Profile</h1>
              <p className="text-sm text-zinc-500 dark:text-zinc-400">{auth.user?.email}</p>
            </div>
          </div>

          <Link
            to="/"
            className="inline-flex items-center gap-2 rounded-xl border border-zinc-200 bg-white px-3 py-2 text-sm font-medium text-zinc-600 transition-colors hover:bg-zinc-100 hover:text-zinc-800 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-300 dark:hover:bg-zinc-700 dark:hover:text-zinc-100"
          >
            <ArrowLeft className="h-4 w-4" />
            Dashboard
          </Link>
        </div>

        <form onSubmit={handleSubmit} className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm dark:border-zinc-800 dark:bg-zinc-900/70">
          {error && (
            <p className="mb-4 rounded-xl border border-rose-200 bg-rose-50 px-4 py-2.5 text-sm text-rose-600 dark:border-rose-500/20 dark:bg-rose-500/10 dark:text-rose-400">
              {error}
            </p>
          )}
          {success && (
            <p className="mb-4 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-2.5 text-sm text-emerald-700 dark:border-emerald-500/20 dark:bg-emerald-500/10 dark:text-emerald-400">
              {success}
            </p>
          )}

          <div className="grid gap-4">
            <label className="grid gap-1.5 text-sm font-medium text-zinc-700 dark:text-zinc-300">
              Name
              <input
                type="text"
                value={name}
                onChange={(event) => setName(event.target.value)}
                required
                className="rounded-xl border border-zinc-200 bg-zinc-50 px-3 py-2.5 text-sm text-zinc-900 outline-none focus:border-sky-400/60 focus:ring-1 focus:ring-sky-400/30 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-100"
              />
            </label>

            <label className="grid gap-1.5 text-sm font-medium text-zinc-700 dark:text-zinc-300">
              Telegram user ID
              <input
                type="text"
                value={telegramUserId}
                onChange={(event) => setTelegramUserId(event.target.value)}
                className="rounded-xl border border-zinc-200 bg-zinc-50 px-3 py-2.5 text-sm text-zinc-900 outline-none focus:border-sky-400/60 focus:ring-1 focus:ring-sky-400/30 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-100"
              />
            </label>

            <label className="grid gap-1.5 text-sm font-medium text-zinc-700 dark:text-zinc-300">
              Phone number
              <input
                type="tel"
                value={phoneNumber}
                onChange={(event) => setPhoneNumber(event.target.value)}
                className="rounded-xl border border-zinc-200 bg-zinc-50 px-3 py-2.5 text-sm text-zinc-900 outline-none focus:border-sky-400/60 focus:ring-1 focus:ring-sky-400/30 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-100"
              />
            </label>

            <label className="grid gap-1.5 text-sm font-medium text-zinc-700 dark:text-zinc-300">
              New password
              <input
                type="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                autoComplete="new-password"
                minLength={8}
                className="rounded-xl border border-zinc-200 bg-zinc-50 px-3 py-2.5 text-sm text-zinc-900 outline-none focus:border-sky-400/60 focus:ring-1 focus:ring-sky-400/30 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-100"
              />
            </label>
          </div>

          <div className="mt-6 flex justify-end">
            <button
              type="submit"
              disabled={submitting}
              className="inline-flex items-center gap-2 rounded-xl bg-sky-500 px-4 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-sky-600 disabled:cursor-not-allowed disabled:opacity-70 dark:hover:bg-sky-400"
            >
              <Save className="h-4 w-4" />
              {submitting ? 'Saving...' : 'Save profile'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
