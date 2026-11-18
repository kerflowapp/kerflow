export type InlineCommit = { kind: 'unchanged' } | { kind: 'invalid' } | { kind: 'save'; value: string };

/** Decides what leaving an inline field does: nothing to save, a refused empty required value, or a save. */
export function resolveInlineCommit(draft: string, original: string | null | undefined, required = false): InlineCommit {
	const value = draft.trim();
	if (value === (original ?? '').trim()) return { kind: 'unchanged' };
	if (required && !value) return { kind: 'invalid' };
	return { kind: 'save', value };
}

/** Same tags in the same order, ignoring surrounding spaces and blank entries. */
export function normalizeTags(tags: string[]): string[] {
	return [...new Set(tags.map(tag => tag.trim()).filter(Boolean))];
}

export function sameTags(a: string[], b: string[]): boolean {
	const left = normalizeTags(a);
	const right = normalizeTags(b);
	return left.length === right.length && left.every((tag, index) => tag === right[index]);
}
