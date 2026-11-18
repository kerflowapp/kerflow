import { describe, expect, it } from 'vitest';

import { normalizeTags, resolveInlineCommit, sameTags } from '../inline-edit';

describe('resolveInlineCommit', () => {
	it('saves a changed value, trimmed', () => {
		expect(resolveInlineCommit('  02 98 12 34 56 ', '02 98 00 00 00')).toEqual({ kind: 'save', value: '02 98 12 34 56' });
	});

	it('ignores a value equal to the original once trimmed', () => {
		expect(resolveInlineCommit('contact@example.com ', 'contact@example.com')).toEqual({ kind: 'unchanged' });
	});

	it('treats a missing original as empty', () => {
		expect(resolveInlineCommit('', null)).toEqual({ kind: 'unchanged' });
		expect(resolveInlineCommit('note', undefined)).toEqual({ kind: 'save', value: 'note' });
	});

	it('saves an empty value to clear an optional field', () => {
		expect(resolveInlineCommit('   ', 'https://example.com')).toEqual({ kind: 'save', value: '' });
	});

	it('refuses to clear a required field', () => {
		expect(resolveInlineCommit('  ', 'Boulangerie', true)).toEqual({ kind: 'invalid' });
	});
});

describe('normalizeTags', () => {
	it('trims, drops blanks and duplicates', () => {
		expect(normalizeTags([' vip ', '', 'vip', 'lead'])).toEqual(['vip', 'lead']);
	});
});

describe('sameTags', () => {
	it('compares normalized lists in order', () => {
		expect(sameTags(['vip ', 'lead'], ['vip', 'lead'])).toBe(true);
		expect(sameTags(['vip'], ['vip', 'lead'])).toBe(false);
		expect(sameTags(['lead', 'vip'], ['vip', 'lead'])).toBe(false);
	});
});
