import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useState, useCallback } from 'react';
import { Search, Sparkles, AlertTriangle, Loader2, ArrowRight, Zap } from 'lucide-react';
import { useSemanticSearch } from '@/hooks/api';
import { normalizeStatus } from '../../enums/work-item-status.enum';
function mapSearchResult(result) {
    const workItem = result.workItem;
    if (!workItem?.workItemId || !workItem.title)
        return null;
    return {
        workItem: {
            id: workItem.workItemId,
            sprintId: workItem.sprintId,
            title: workItem.title,
            description: workItem.description,
            type: (workItem.workType ?? 'TASK'),
            status: normalizeStatus(workItem.status),
            priority: (workItem.priority ?? 'MEDIUM'),
            externalLink: workItem.externalLink,
            estimatedMinutes: workItem.estimatedMinutes,
            totalLoggedMinutes: 0,
            dueDate: workItem.dueDate ? String(workItem.dueDate).slice(0, 10) : undefined,
            createdAt: workItem.createdAt ?? new Date().toISOString(),
            updatedAt: workItem.updatedAt ?? workItem.createdAt ?? new Date().toISOString(),
            completedAt: workItem.completedAt,
            createdBy: {
                userId: workItem.createdByUserId ?? 'system',
                name: workItem.createdByUserId ?? 'System',
            },
            assignees: [],
            tags: [],
        },
        relevanceScore: result.relevanceScore ?? 0,
    };
}
function mapSearchResponse(response, fallbackQuery) {
    return {
        query: response.query ?? fallbackQuery,
        totalResults: response.totalResults ?? 0,
        aiAvailable: response.aiAvailable ?? false,
        fallbackMessage: response.fallbackMessage ?? null,
        results: (response.results ?? [])
            .map(mapSearchResult)
            .filter((result) => Boolean(result)),
    };
}
/**
 * SemanticSearchPanel — RF-005 Semantic Task Search UI.
 *
 * Provides a natural-language search bar powered by Alibaba Cloud
 * DashScope embeddings. Shows results ranked by relevance with
 * visual score badges. Gracefully degrades to keyword search if
 * AI is unavailable.
 */
export function SemanticSearchPanel({ onViewDetail, }) {
    const [query, setQuery] = useState('');
    const [results, setResults] = useState(null);
    const [error, setError] = useState(null);
    const searchMutation = useSemanticSearch();
    const loading = searchMutation.isPending;
    const handleSearch = useCallback(async () => {
        const trimmed = query.trim();
        if (!trimmed || trimmed.length < 2)
            return;
        setError(null);
        try {
            const res = await searchMutation.mutateAsync({ query: trimmed, maxResults: 10 });
            setResults(mapSearchResponse(res, trimmed));
        }
        catch (err) {
            setError('An unexpected error occurred');
        }
    }, [query, searchMutation]);
    const handleKeyDown = useCallback((e) => {
        if (e.key === 'Enter')
            handleSearch();
    }, [handleSearch]);
    return (_jsxs("div", { className: "space-y-6", children: [_jsxs("div", { className: "relative", children: [_jsxs("div", { className: "flex items-center gap-3 rounded-2xl border border-zinc-200 bg-white px-4 py-3 shadow-sm transition-all focus-within:border-violet-400 focus-within:ring-2 focus-within:ring-violet-400/20 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:focus-within:border-violet-500 dark:focus-within:ring-violet-500/20", children: [_jsx(Sparkles, { className: "h-5 w-5 flex-shrink-0 text-violet-500" }), _jsx("input", { id: "semantic-search-input", type: "text", value: query, onChange: (e) => setQuery(e.target.value), onKeyDown: handleKeyDown, placeholder: "Describe what you're looking for\u2026 e.g. \"critical bugs in the login flow\"", className: "flex-1 bg-transparent text-sm text-zinc-800 placeholder-zinc-400 outline-none dark:text-zinc-200 dark:placeholder-zinc-500" }), _jsxs("button", { id: "semantic-search-button", type: "button", onClick: handleSearch, disabled: loading || query.trim().length < 2, className: "flex items-center gap-2 rounded-xl bg-violet-500 px-4 py-2 text-sm font-medium text-white transition-all hover:bg-violet-600 disabled:cursor-not-allowed disabled:opacity-50 dark:bg-violet-600 dark:hover:bg-violet-500", children: [loading ? (_jsx(Loader2, { className: "h-4 w-4 animate-spin" })) : (_jsx(Search, { className: "h-4 w-4" })), "Search"] })] }), _jsx("p", { className: "mt-2 text-xs text-zinc-400 dark:text-zinc-500", children: "Powered by AI semantic embeddings \u2014 search by meaning, not just keywords." })] }), error && (_jsxs("div", { className: "flex items-start gap-3 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700 dark:border-red-800/60 dark:bg-red-900/20 dark:text-red-300", children: [_jsx(AlertTriangle, { className: "mt-0.5 h-4 w-4 flex-shrink-0" }), _jsx("span", { children: error })] })), results && !results.aiAvailable && results.fallbackMessage && (_jsxs("div", { className: "flex items-start gap-3 rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-700 dark:border-amber-800/60 dark:bg-amber-900/20 dark:text-amber-300", children: [_jsx(AlertTriangle, { className: "mt-0.5 h-4 w-4 flex-shrink-0" }), _jsx("span", { children: results.fallbackMessage })] })), loading && (_jsxs("div", { className: "flex flex-col items-center justify-center py-16", children: [_jsx("div", { className: "mb-3 h-10 w-10 animate-spin rounded-full border-2 border-zinc-200 border-t-violet-500 dark:border-zinc-700 dark:border-t-violet-400" }), _jsx("p", { className: "text-sm text-zinc-500 dark:text-zinc-400", children: "Analyzing your query with AI\u2026" })] })), !loading && results && (_jsxs("div", { className: "space-y-3", children: [_jsxs("div", { className: "flex items-center justify-between", children: [_jsxs("h3", { className: "text-sm font-semibold text-zinc-700 dark:text-zinc-300", children: [results.totalResults, " result", results.totalResults !== 1 ? 's' : '', " for \"", results.query, "\""] }), results.aiAvailable && (_jsxs("span", { className: "flex items-center gap-1 rounded-full bg-violet-100 px-2.5 py-0.5 text-xs font-medium text-violet-700 dark:bg-violet-900/30 dark:text-violet-300", children: [_jsx(Zap, { className: "h-3 w-3" }), "AI-Powered"] }))] }), results.results.length === 0 && (_jsxs("div", { className: "rounded-xl border border-zinc-200 bg-zinc-50 py-12 text-center dark:border-zinc-800 dark:bg-zinc-900/40", children: [_jsx(Search, { className: "mx-auto mb-3 h-8 w-8 text-zinc-300 dark:text-zinc-600" }), _jsx("p", { className: "text-sm text-zinc-500 dark:text-zinc-400", children: "No matching tasks found. Try rephrasing your query." })] })), results.results.map((result) => (_jsx(SearchResultCard, { result: result, onViewDetail: onViewDetail }, result.workItem.id)))] }))] }));
}
// ─── Sub-components ──────────────────────────────────────────────
function SearchResultCard({ result, onViewDetail, }) {
    const { workItem, relevanceScore } = result;
    const percent = Math.round(relevanceScore * 100);
    const scoreColor = percent >= 80
        ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-300'
        : percent >= 50
            ? 'bg-sky-100 text-sky-700 dark:bg-sky-900/30 dark:text-sky-300'
            : 'bg-zinc-100 text-zinc-600 dark:bg-zinc-800 dark:text-zinc-400';
    const priorityBadge = {
        HIGH: 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-300',
        CRITICAL: 'bg-red-200 text-red-800 dark:bg-red-900/40 dark:text-red-200',
        MEDIUM: 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-300',
        LOW: 'bg-zinc-100 text-zinc-600 dark:bg-zinc-800 dark:text-zinc-400',
    };
    const statusBadge = {
        TODO: 'bg-zinc-100 text-zinc-600 dark:bg-zinc-800 dark:text-zinc-400',
        'IN_PROGRESS': 'bg-sky-100 text-sky-700 dark:bg-sky-900/30 dark:text-sky-300',
        DONE: 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-300',
        NEW: 'bg-violet-100 text-violet-700 dark:bg-violet-900/30 dark:text-violet-300',
    };
    return (_jsx("div", { className: "group cursor-pointer rounded-xl border border-zinc-200 bg-white p-4 transition-all hover:border-violet-300 hover:shadow-md dark:border-zinc-800 dark:bg-zinc-900/60 dark:hover:border-violet-700", onClick: () => onViewDetail?.(workItem), children: _jsxs("div", { className: "flex items-start justify-between gap-3", children: [_jsxs("div", { className: "min-w-0 flex-1", children: [_jsx("h4", { className: "mb-1 truncate text-sm font-semibold text-zinc-800 group-hover:text-violet-600 dark:text-zinc-200 dark:group-hover:text-violet-400", children: workItem.title }), workItem.description && (_jsx("p", { className: "mb-2 line-clamp-2 text-xs text-zinc-500 dark:text-zinc-400", children: workItem.description })), _jsxs("div", { className: "flex flex-wrap items-center gap-2", children: [_jsx("span", { className: `inline-flex items-center rounded-full px-2 py-0.5 text-[10px] font-medium ${statusBadge[workItem.status] ?? statusBadge.TODO}`, children: workItem.status }), _jsx("span", { className: `inline-flex items-center rounded-full px-2 py-0.5 text-[10px] font-medium ${priorityBadge[workItem.priority] ?? priorityBadge.MEDIUM}`, children: workItem.priority }), _jsx("span", { className: "text-[10px] text-zinc-400 dark:text-zinc-500", children: workItem.type })] })] }), _jsxs("div", { className: "flex flex-col items-end gap-2", children: [_jsxs("span", { className: `inline-flex items-center gap-1 rounded-full px-2.5 py-1 text-xs font-bold ${scoreColor}`, children: [percent, "%"] }), _jsx(ArrowRight, { className: "h-4 w-4 text-zinc-300 transition-transform group-hover:translate-x-0.5 group-hover:text-violet-500 dark:text-zinc-600 dark:group-hover:text-violet-400" })] })] }) }));
}
