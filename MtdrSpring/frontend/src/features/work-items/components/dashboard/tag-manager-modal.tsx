import React, { useEffect, useMemo, useState } from 'react';
import { Edit3, Loader2, Plus, Tags, Trash2, X } from 'lucide-react';
import { useTagCreate, useTagDelete, useTagUpdate } from '@/hooks/api';
import type { TagDto } from '@/shared/dtos/tag.dto';

const HEX_COLOR_PATTERN = /^#[0-9A-Fa-f]{6}$/;
const DEFAULT_TAG_COLOR = '#3B82F6';
const TAG_DESCRIPTION_MAX_LENGTH = 300;
const COLOR_SWATCHES = [
  '#EF4444',
  '#F97316',
  '#F59E0B',
  '#EAB308',
  '#84CC16',
  '#22C55E',
  '#10B981',
  '#14B8A6',
  '#06B6D4',
  '#0EA5E9',
  '#3B82F6',
  '#6366F1',
  '#8B5CF6',
  '#A855F7',
  '#D946EF',
  '#EC4899',
];

type Mode = 'list' | 'create' | 'edit';

interface ResponseError {
  status: number;
  message: string;
  error: string;
}

interface TagManagerModalProps {
  isOpen: boolean;
  tags: TagDto[];
  onClose: () => void;
}

interface FormState {
  name: string;
  color: string;
  description: string;
}

const EMPTY_FORM: FormState = {
  name: '',
  color: DEFAULT_TAG_COLOR,
  description: '',
};

function tagTextColor(color: string) {
  const normalized = HEX_COLOR_PATTERN.test(color) ? color.slice(1) : DEFAULT_TAG_COLOR.slice(1);
  const r = parseInt(normalized.slice(0, 2), 16);
  const g = parseInt(normalized.slice(2, 4), 16);
  const b = parseInt(normalized.slice(4, 6), 16);
  return (r * 299 + g * 587 + b * 114) / 1000 > 155 ? '#1F2937' : '#FFFFFF';
}

function Label({ children }: { children: React.ReactNode }) {
  return (
    <label className="mb-1.5 block text-sm font-medium text-zinc-600 dark:text-zinc-300">
      {children}
    </label>
  );
}

export function TagManagerModal({ isOpen, tags, onClose }: TagManagerModalProps) {
  const createTagMutation = useTagCreate();
  const updateTagMutation = useTagUpdate();
  const deleteTagMutation = useTagDelete();
  const [mode, setMode] = useState<Mode>('list');
  const [editingTag, setEditingTag] = useState<TagDto | null>(null);
  const [form, setForm] = useState<FormState>(EMPTY_FORM);
  const [error, setError] = useState('');

  const title = mode === 'edit' ? 'Edit Tag' : mode === 'create' ? 'New Tag' : 'Manage Tags';
  const saving = createTagMutation.isPending || updateTagMutation.isPending;
  const deletingId = useMemo(() => {
    const variables = deleteTagMutation.variables;
    return deleteTagMutation.isPending && typeof variables === 'string' ? variables : null;
  }, [deleteTagMutation.isPending, deleteTagMutation.variables]);

  useEffect(() => {
    if (!isOpen) return;
    setMode('list');
    setEditingTag(null);
    setForm(EMPTY_FORM);
    setError('');
  }, [isOpen]);

  function openCreate() {
    setMode('create');
    setEditingTag(null);
    setForm(EMPTY_FORM);
    setError('');
  }

  function openEdit(tag: TagDto) {
    setMode('edit');
    setEditingTag(tag);
    setForm({
      name: tag.name,
      color: tag.color,
      description: tag.description ?? '',
    });
    setError('');
  }

  function backToList() {
    setMode('list');
    setEditingTag(null);
    setForm(EMPTY_FORM);
    setError('');
  }

  function set<K extends keyof FormState>(key: K, value: FormState[K]) {
    setForm((prev) => ({ ...prev, [key]: value }));
  }

  async function saveTag() {
    const name = form.name.trim();
    const description = form.description.trim();
    const color = form.color.trim().toUpperCase();

    if (!name) {
      setError('Tag name is required.');
      return;
    }
    if (!HEX_COLOR_PATTERN.test(color)) {
      setError('Color must be a valid HEX value, for example #3B82F6.');
      return;
    }
    if (description.length > TAG_DESCRIPTION_MAX_LENGTH) {
      setError(`Description must be ${TAG_DESCRIPTION_MAX_LENGTH} characters or fewer.`);
      return;
    }

    setError('');
    try {
      if (mode === 'edit' && editingTag) {
        await updateTagMutation.mutateAsync({
          id: editingTag.id,
          body: { name, color, description: description || undefined },
        });
      } else {
        await createTagMutation.mutateAsync({ name, color, description: description || undefined });
      }
      backToList();
    } catch(e: any) {
      setError(mode === 'edit' ? `Could not update tag: ${e.message}` : `Could not create tag: ${e.message}`);
    }
  }

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    await saveTag();
  }

  async function handleDelete(tag: TagDto) {
    const confirmed = window.confirm(`Delete "${tag.name}"? This cannot be undone.`);
    if (!confirmed) return;

    setError('');
    try {
      await deleteTagMutation.mutateAsync(tag.id);
    } catch {
      setError('Could not delete tag. Please try again.');
    }
  }

  if (!isOpen) return null;

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-label={title}
      className="fixed inset-0 z-50 flex items-center justify-center p-4"
    >
      <div className="absolute inset-0 bg-black/50 backdrop-blur-sm dark:bg-black/70" onClick={onClose} />

      <div className="relative z-10 flex max-h-[88vh] w-full max-w-5xl flex-col rounded-2xl border border-zinc-200 bg-white shadow-xl dark:border-zinc-700 dark:bg-zinc-900 dark:shadow-2xl">
        <div className="flex items-center justify-between border-b border-zinc-100 px-6 py-5 dark:border-zinc-800">
          <h2 className="text-xl font-bold text-zinc-900 dark:text-zinc-100">{title}</h2>
          <button
            type="button"
            onClick={onClose}
            className="rounded-lg p-1.5 text-zinc-400 transition-colors hover:bg-zinc-100 hover:text-zinc-700 dark:text-zinc-500 dark:hover:bg-zinc-800 dark:hover:text-zinc-200"
            aria-label="Close tags modal"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        <div className="min-h-0 flex-1 overflow-y-auto p-6">
          {error && (
            <p className="mb-4 rounded-xl border border-rose-200 bg-rose-50 px-4 py-2.5 text-sm text-rose-600 dark:border-rose-500/20 dark:bg-rose-500/10 dark:text-rose-400">
              {error}
            </p>
          )}

          {mode === 'list' ? (
            <div className="space-y-3">
              {tags.length === 0 && (
                <div className="flex flex-col items-center justify-center rounded-xl border border-dashed border-zinc-200 px-6 py-12 text-center dark:border-zinc-700">
                  <Tags className="h-8 w-8 text-zinc-300 dark:text-zinc-600" />
                  <p className="mt-3 text-sm font-medium text-zinc-600 dark:text-zinc-300">No tags yet</p>
                </div>
              )}

              {tags.map((tag) => (
                <div
                  key={tag.id}
                  className="flex items-center gap-4 rounded-xl border border-zinc-200 bg-white px-4 py-4 dark:border-zinc-800 dark:bg-zinc-900/60"
                >
                  <span
                    className="inline-flex min-w-24 max-w-52 items-center justify-center truncate rounded-full px-3 py-1.5 text-sm font-semibold"
                    style={{ backgroundColor: tag.color, color: tagTextColor(tag.color) }}
                    title={tag.name}
                  >
                    {tag.name}
                  </span>
                  <div className="min-w-0 flex-1">
                    <p className="truncate text-sm font-semibold text-zinc-700 dark:text-zinc-200">{tag.name}</p>
                    {tag.description && (
                      <p className="mt-0.5 truncate text-xs text-zinc-400 dark:text-zinc-500">{tag.description}</p>
                    )}
                  </div>
                  <button
                    type="button"
                    onClick={() => openEdit(tag)}
                    className="rounded-lg p-2 text-blue-600 transition-colors hover:bg-blue-50 hover:text-blue-700 dark:text-blue-400 dark:hover:bg-blue-500/10 dark:hover:text-blue-300"
                    aria-label={`Edit ${tag.name}`}
                    title="Edit tag"
                  >
                    <Edit3 className="h-5 w-5" />
                  </button>
                  <button
                    type="button"
                    onClick={() => handleDelete(tag)}
                    disabled={deletingId === tag.id}
                    className="rounded-lg p-2 text-red-600 transition-colors hover:bg-red-50 hover:text-red-700 disabled:opacity-50 dark:text-red-400 dark:hover:bg-red-500/10 dark:hover:text-red-300"
                    aria-label={`Delete ${tag.name}`}
                    title="Delete tag"
                  >
                    {deletingId === tag.id ? <Loader2 className="h-5 w-5 animate-spin" /> : <Trash2 className="h-5 w-5" />}
                  </button>
                </div>
              ))}
            </div>
          ) : (
            <form onSubmit={handleSubmit} className="grid gap-5">
              <div>
                <Label>Name *</Label>
                <input
                  type="text"
                  value={form.name}
                  onChange={(event) => set('name', event.target.value)}
                  className="w-full rounded-xl border border-zinc-200 bg-zinc-50 px-3 py-2.5 text-sm text-zinc-800 outline-none focus:border-sky-400/60 focus:ring-1 focus:ring-sky-400/30 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-200 dark:focus:border-sky-500/60 dark:focus:ring-sky-500/30"
                  autoFocus
                />
              </div>

              <div>
                <Label>Color *</Label>
                <div className="grid grid-cols-8 gap-2 sm:w-fit">
                  {COLOR_SWATCHES.map((color) => {
                    const selected = form.color.toUpperCase() === color;
                    return (
                      <button
                        key={color}
                        type="button"
                        onClick={() => set('color', color)}
                        className={`h-9 w-9 rounded-xl transition-all ${selected ? 'ring-2 ring-sky-500 ring-offset-2 dark:ring-offset-zinc-900' : 'hover:scale-105'}`}
                        style={{ backgroundColor: color }}
                        aria-label={`Pick ${color}`}
                        title={color}
                      />
                    );
                  })}
                </div>
                <input
                  type="text"
                  value={form.color}
                  onChange={(event) => set('color', event.target.value.toUpperCase())}
                  className="mt-3 w-full rounded-xl border border-zinc-200 bg-zinc-50 px-3 py-2.5 text-sm text-zinc-800 outline-none focus:border-sky-400/60 focus:ring-1 focus:ring-sky-400/30 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-200 dark:focus:border-sky-500/60 dark:focus:ring-sky-500/30"
                  placeholder="#3B82F6"
                />
              </div>

              <div>
                <Label>Description</Label>
                <textarea
                  value={form.description}
                  onChange={(event) => set('description', event.target.value)}
                  maxLength={TAG_DESCRIPTION_MAX_LENGTH}
                  rows={4}
                  className="w-full resize-none rounded-xl border border-zinc-200 bg-zinc-50 px-3 py-2.5 text-sm text-zinc-800 outline-none focus:border-sky-400/60 focus:ring-1 focus:ring-sky-400/30 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-200 dark:focus:border-sky-500/60 dark:focus:ring-sky-500/30"
                />
                <p className="mt-1 text-right text-[11px] text-zinc-400 dark:text-zinc-500">
                  {form.description.length}/{TAG_DESCRIPTION_MAX_LENGTH}
                </p>
              </div>

              <div>
                <Label>Preview</Label>
                <span
                  className="inline-flex max-w-full items-center rounded-full px-3 py-1.5 text-sm font-semibold"
                  style={{ backgroundColor: form.color, color: tagTextColor(form.color) }}
                >
                  {form.name.trim() || 'Tag name'}
                </span>
              </div>
            </form>
          )}
        </div>

        <div className="flex items-center justify-between border-t border-zinc-100 px-6 py-4 dark:border-zinc-800">
          {mode === 'list' ? (
            <>
              <button
                type="button"
                onClick={openCreate}
                className="inline-flex items-center gap-2 rounded-xl bg-sky-500 px-4 py-2 text-sm font-semibold text-white transition-colors hover:bg-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-500/50 dark:hover:bg-sky-400"
              >
                <Plus className="h-4 w-4" />
                New Tag
              </button>
              <p className="text-sm font-semibold text-zinc-500 dark:text-zinc-400">
                {tags.length} tag{tags.length === 1 ? '' : 's'}
              </p>
            </>
          ) : (
            <div className="ml-auto flex items-center gap-3">
              <button
                type="button"
                onClick={backToList}
                className="rounded-xl border border-zinc-200 bg-zinc-50 px-4 py-2 text-sm font-medium text-zinc-700 transition-colors hover:bg-zinc-100 hover:text-zinc-900 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-300 dark:hover:bg-zinc-700 dark:hover:text-zinc-100"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={saveTag}
                disabled={saving}
                className="inline-flex items-center gap-2 rounded-xl bg-sky-500 px-5 py-2 text-sm font-semibold text-white transition-colors hover:bg-sky-600 disabled:opacity-50 dark:hover:bg-sky-400"
              >
                {saving && <Loader2 className="h-4 w-4 animate-spin" />}
                {mode === 'edit' ? 'Save Changes' : 'Create Tag'}
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
