import { useState, useCallback } from 'react';
import { Search, Sparkles, AlertTriangle, Loader2, ArrowRight, Zap } from 'lucide-react';
import { semanticSearchService } from '../../services/semantic-search.service';
import type { SemanticSearchResult, SemanticSearchResponse } from '../../services/semantic-search.service';
import type { WorkItemDetailDto } from '../../dtos/work-item-detail.dto';

/**
 * SemanticSearchPanel — RF-005 Semantic Task Search UI.
 *
 * Provides a natural-language search bar powered by Alibaba Cloud
 * DashScope embeddings. Shows results ranked by relevance with
 * visual score badges. Gracefully degrades to keyword search if
 * AI is unavailable.
 */
export function SemanticSearchPanel({
    onViewDetail,
}: {
    onViewDetail?: (item: WorkItemDetailDto) => void;
}) {
    const [query, setQuery] = useState('');
    const [loading, setLoading] = useState(false);
    const [results, setResults] = useState<SemanticSearchResponse | null>(null);
    const [error, setError] = useState<string | null>(null);

    const handleSearch = useCallback(async () => {
        const trimmed = query.trim();
        if (!trimmed || trimmed.length < 2) return;

        setLoading(true);
        setError(null);

        try {
            const res = await semanticSearchService.search(trimmed, 10);
            if (res.success) {
                setResults(res.data);
            } else {
                setError(res.message ?? 'Search failed');
            }
        } catch (err) {
            setError('An unexpected error occurred');
        } finally {
            setLoading(false);
        }
    }, [query]);

    const handleKeyDown = useCallback(
        (e: React.KeyboardEvent) => {
            if (e.key === 'Enter') handleSearch();
        },
        [handleSearch]
    );

    return (
        <div className="space-y-6">
            {/* Search input */}
            <div className="relative">
                <div className="flex items-center gap-3 rounded-2xl border border-zinc-200 bg-white px-4 py-3 shadow-sm transition-all focus-within:border-violet-400 focus-within:ring-2 focus-within:ring-violet-400/20 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:focus-within:border-violet-500 dark:focus-within:ring-violet-500/20">
                    <Sparkles className="h-5 w-5 flex-shrink-0 text-violet-500" />
                    <input
                        id="semantic-search-input"
                        type="text"
                        value={query}
                        onChange={(e) => setQuery(e.target.value)}
                        onKeyDown={handleKeyDown}
                        placeholder="Describe what you're looking for… e.g. &quot;critical bugs in the login flow&quot;"
                        className="flex-1 bg-transparent text-sm text-zinc-800 placeholder-zinc-400 outline-none dark:text-zinc-200 dark:placeholder-zinc-500"
                    />
                    <button
                        id="semantic-search-button"
                        type="button"
                        onClick={handleSearch}
                        disabled={loading || query.trim().length < 2}
                        className="flex items-center gap-2 rounded-xl bg-violet-500 px-4 py-2 text-sm font-medium text-white transition-all hover:bg-violet-600 disabled:cursor-not-allowed disabled:opacity-50 dark:bg-violet-600 dark:hover:bg-violet-500"
                    >
                        {loading ? (
                            <Loader2 className="h-4 w-4 animate-spin" />
                        ) : (
                            <Search className="h-4 w-4" />
                        )}
                        Search
                    </button>
                </div>
                <p className="mt-2 text-xs text-zinc-400 dark:text-zinc-500">
                    Powered by AI semantic embeddings — search by meaning, not just keywords.
                </p>
            </div>

            {/* Error */}
            {error && (
                <div className="flex items-start gap-3 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700 dark:border-red-800/60 dark:bg-red-900/20 dark:text-red-300">
                    <AlertTriangle className="mt-0.5 h-4 w-4 flex-shrink-0" />
                    <span>{error}</span>
                </div>
            )}

            {/* Fallback warning */}
            {results && !results.aiAvailable && results.fallbackMessage && (
                <div className="flex items-start gap-3 rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-700 dark:border-amber-800/60 dark:bg-amber-900/20 dark:text-amber-300">
                    <AlertTriangle className="mt-0.5 h-4 w-4 flex-shrink-0" />
                    <span>{results.fallbackMessage}</span>
                </div>
            )}

            {/* Loading */}
            {loading && (
                <div className="flex flex-col items-center justify-center py-16">
                    <div className="mb-3 h-10 w-10 animate-spin rounded-full border-2 border-zinc-200 border-t-violet-500 dark:border-zinc-700 dark:border-t-violet-400" />
                    <p className="text-sm text-zinc-500 dark:text-zinc-400">
                        Analyzing your query with AI…
                    </p>
                </div>
            )}

            {/* Results */}
            {!loading && results && (
                <div className="space-y-3">
                    {/* Header */}
                    <div className="flex items-center justify-between">
                        <h3 className="text-sm font-semibold text-zinc-700 dark:text-zinc-300">
                            {results.totalResults} result{results.totalResults !== 1 ? 's' : ''} for "{results.query}"
                        </h3>
                        {results.aiAvailable && (
                            <span className="flex items-center gap-1 rounded-full bg-violet-100 px-2.5 py-0.5 text-xs font-medium text-violet-700 dark:bg-violet-900/30 dark:text-violet-300">
                                <Zap className="h-3 w-3" />
                                AI-Powered
                            </span>
                        )}
                    </div>

                    {/* Empty state */}
                    {results.results.length === 0 && (
                        <div className="rounded-xl border border-zinc-200 bg-zinc-50 py-12 text-center dark:border-zinc-800 dark:bg-zinc-900/40">
                            <Search className="mx-auto mb-3 h-8 w-8 text-zinc-300 dark:text-zinc-600" />
                            <p className="text-sm text-zinc-500 dark:text-zinc-400">
                                No matching tasks found. Try rephrasing your query.
                            </p>
                        </div>
                    )}

                    {/* Result cards */}
                    {results.results.map((result) => (
                        <SearchResultCard
                            key={result.workItem.id}
                            result={result}
                            onViewDetail={onViewDetail}
                        />
                    ))}
                </div>
            )}
        </div>
    );
}

// ─── Sub-components ──────────────────────────────────────────────

function SearchResultCard({
    result,
    onViewDetail,
}: {
    result: SemanticSearchResult;
    onViewDetail?: (item: WorkItemDetailDto) => void;
}) {
    const { workItem, relevanceScore } = result;
    const percent = Math.round(relevanceScore * 100);

    const scoreColor =
        percent >= 80
            ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-300'
            : percent >= 50
              ? 'bg-sky-100 text-sky-700 dark:bg-sky-900/30 dark:text-sky-300'
              : 'bg-zinc-100 text-zinc-600 dark:bg-zinc-800 dark:text-zinc-400';

    const priorityBadge: Record<string, string> = {
        HIGH: 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-300',
        CRITICAL: 'bg-red-200 text-red-800 dark:bg-red-900/40 dark:text-red-200',
        MEDIUM: 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-300',
        LOW: 'bg-zinc-100 text-zinc-600 dark:bg-zinc-800 dark:text-zinc-400',
    };

    const statusBadge: Record<string, string> = {
        TODO: 'bg-zinc-100 text-zinc-600 dark:bg-zinc-800 dark:text-zinc-400',
        'IN_PROGRESS': 'bg-sky-100 text-sky-700 dark:bg-sky-900/30 dark:text-sky-300',
        DONE: 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-300',
        NEW: 'bg-violet-100 text-violet-700 dark:bg-violet-900/30 dark:text-violet-300',
    };

    return (
        <div
            className="group cursor-pointer rounded-xl border border-zinc-200 bg-white p-4 transition-all hover:border-violet-300 hover:shadow-md dark:border-zinc-800 dark:bg-zinc-900/60 dark:hover:border-violet-700"
            onClick={() => onViewDetail?.(workItem)}
        >
            <div className="flex items-start justify-between gap-3">
                <div className="min-w-0 flex-1">
                    {/* Title */}
                    <h4 className="mb-1 truncate text-sm font-semibold text-zinc-800 group-hover:text-violet-600 dark:text-zinc-200 dark:group-hover:text-violet-400">
                        {workItem.title}
                    </h4>

                    {/* Description preview */}
                    {workItem.description && (
                        <p className="mb-2 line-clamp-2 text-xs text-zinc-500 dark:text-zinc-400">
                            {workItem.description}
                        </p>
                    )}

                    {/* Badges */}
                    <div className="flex flex-wrap items-center gap-2">
                        <span
                            className={`inline-flex items-center rounded-full px-2 py-0.5 text-[10px] font-medium ${statusBadge[workItem.status] ?? statusBadge.TODO}`}
                        >
                            {workItem.status}
                        </span>
                        <span
                            className={`inline-flex items-center rounded-full px-2 py-0.5 text-[10px] font-medium ${priorityBadge[workItem.priority] ?? priorityBadge.MEDIUM}`}
                        >
                            {workItem.priority}
                        </span>
                        <span className="text-[10px] text-zinc-400 dark:text-zinc-500">
                            {workItem.type}
                        </span>
                    </div>
                </div>

                {/* Relevance score + action */}
                <div className="flex flex-col items-end gap-2">
                    <span
                        className={`inline-flex items-center gap-1 rounded-full px-2.5 py-1 text-xs font-bold ${scoreColor}`}
                    >
                        {percent}%
                    </span>
                    <ArrowRight className="h-4 w-4 text-zinc-300 transition-transform group-hover:translate-x-0.5 group-hover:text-violet-500 dark:text-zinc-600 dark:group-hover:text-violet-400" />
                </div>
            </div>
        </div>
    );
}
