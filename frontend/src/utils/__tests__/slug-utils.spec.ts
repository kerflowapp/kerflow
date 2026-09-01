import { describe, expect, it } from 'vitest';

import { createCodeToSlugMap, createSlugMap, nameToSlug, slugToDisplayName } from '../slug-utils';

describe('nameToSlug', () => {
	it('strips accents and lowercases', () => {
		expect(nameToSlug('Île-de-France')).toBe('ile-de-france');
	});

	it('turns apostrophes and spaces into single hyphens', () => {
		expect(nameToSlug("Provence-Alpes-Côte d'Azur")).toBe('provence-alpes-cote-d-azur');
	});

	it('trims leading and trailing hyphens', () => {
		expect(nameToSlug(" -Côtes-d'Armor- ")).toBe('cotes-d-armor');
	});

	it('returns an empty string for empty input', () => {
		expect(nameToSlug('')).toBe('');
	});
});

describe('slugToDisplayName', () => {
	it('capitalises each segment', () => {
		expect(slugToDisplayName('ile-de-france')).toBe('Ile De France');
	});

	it('returns an empty string for empty input', () => {
		expect(slugToDisplayName('')).toBe('');
	});
});

describe('slug maps', () => {
	const source = { '35': 'Ille-et-Vilaine', '75': 'Paris' };

	it('indexes entries by slug', () => {
		const map = createSlugMap(source);
		expect(map.get('ille-et-vilaine')).toEqual({ code: '35', name: 'Ille-et-Vilaine', slug: 'ille-et-vilaine' });
	});

	it('builds the reverse code to slug lookup', () => {
		expect(createCodeToSlugMap(source).get('75')).toBe('paris');
	});
});
