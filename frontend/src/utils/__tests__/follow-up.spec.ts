import { describe, expect, it } from 'vitest';

import type { ProspectDto } from '~/api/dtos/prospect.dto';

import { followUpStateOf } from '../follow-up';

const NOW = new Date(2026, 9, 10, 9, 0);

const waitingSince = (followUpSince: string | null) => ({ followUpSince }) as ProspectDto;
const daysAgo = (days: number, hour = 18) => new Date(2026, 9, 10 - days, hour, 0).toISOString();

describe('followUpStateOf', () => {
	it('is none when the backend says no follow-up applies', () => {
		expect(followUpStateOf(waitingSince(null), 5, NOW).status).toBe('none');
	});

	it('is upcoming while the delay is further than tomorrow', () => {
		expect(followUpStateOf(waitingSince(daysAgo(2)), 5, NOW)).toMatchObject({
			status: 'upcoming',
			daysWaiting: 2,
			daysOverdue: -3
		});
	});

	it('gives the day the follow-up becomes due', () => {
		const { dueDate } = followUpStateOf(waitingSince(daysAgo(2)), 5, NOW);
		expect(dueDate?.getDate()).toBe(13);
	});

	it('is soon the day before it becomes due', () => {
		expect(followUpStateOf(waitingSince(daysAgo(4)), 5, NOW)).toMatchObject({ status: 'soon', daysOverdue: -1 });
	});

	it('is due on the day the delay is reached, whatever the hour', () => {
		expect(followUpStateOf(waitingSince(daysAgo(5, 23)), 5, NOW)).toMatchObject({
			status: 'due',
			daysWaiting: 5,
			daysOverdue: 0
		});
	});

	it('counts the days past the delay', () => {
		expect(followUpStateOf(waitingSince(daysAgo(12)), 5, NOW)).toMatchObject({ status: 'due', daysOverdue: 7 });
	});
});
