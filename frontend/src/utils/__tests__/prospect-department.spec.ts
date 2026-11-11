import { describe, expect, it } from 'vitest';

import { departmentOf } from '../prospect-department';

describe('departmentOf', () => {
	it('reads the department from the postcode of a formatted address', () => {
		expect(departmentOf({ address: '12 Rue de la Paix, 35000 Rennes, France' })).toBe('35');
	});

	it('keeps the leading zero of low department codes', () => {
		expect(departmentOf({ address: '1 Place du Marché, 01000 Bourg-en-Bresse' })).toBe('01');
	});

	it('maps overseas postcodes to their three-digit department', () => {
		expect(departmentOf({ address: '5 Rue Victor Hugo, 97400 Saint-Denis, La Réunion' })).toBe('974');
	});

	it('splits Corsica into 2A and 2B', () => {
		expect(departmentOf({ address: 'Cours Napoléon, 20000 Ajaccio' })).toBe('2A');
		expect(departmentOf({ address: 'Boulevard Paoli, 20200 Bastia' })).toBe('2B');
	});

	it('ignores street numbers that are not postcodes', () => {
		expect(departmentOf({ address: '123456 Unknown Road, 56100 Lorient' })).toBe('56');
	});

	it('is undefined without a postcode', () => {
		expect(departmentOf({ address: 'Rennes, France' })).toBeUndefined();
		expect(departmentOf({ address: '' })).toBeUndefined();
	});
});
