import { addDays, differenceInCalendarDays, parseISO } from 'date-fns';

import type { ProspectDto } from '~/api/dtos/prospect.dto';
import { daysSince } from '~/utils/dateUtils';

/** Mirrors FollowUpService.DEFAULT_DELAY_DAYS, used until the user context is loaded */
export const DEFAULT_FOLLOW_UP_DELAY_DAYS = 5;
export const MIN_FOLLOW_UP_DELAY_DAYS = 1;
export const MAX_FOLLOW_UP_DELAY_DAYS = 90;

export type FollowUpStatus = 'none' | 'upcoming' | 'soon' | 'due';

export type FollowUpState = {
	status: FollowUpStatus;
	/** The day was set by hand rather than computed from the wait for a reply */
	manual: boolean;
	/** 0 when the day was set by hand */
	daysWaiting: number;
	/** 0 on the day the follow-up becomes due, negative before */
	daysOverdue: number;
	/** Day the follow-up becomes due; null when no follow-up applies */
	dueDate: Date | null;
};

const NONE: FollowUpState = { status: 'none', manual: false, daysWaiting: 0, daysOverdue: 0, dueDate: null };

const statusOf = (daysOverdue: number): FollowUpStatus =>
	daysOverdue >= 0 ? 'due' : daysOverdue === -1 ? 'soon' : 'upcoming';

/**
 * A day set by hand (followUpDate) wins, in any column. Otherwise the backend decides whether a
 * prospect is waiting for a reply (followUpSince) and this only compares that date with the user's
 * delay. "soon" means due tomorrow, "upcoming" later on.
 */
export const followUpStateOf = (prospect: ProspectDto, delayDays: number, now: Date = new Date()): FollowUpState => {
	if (prospect.followUpDate) {
		// parseISO reads a date-only string as local time; new Date() would read it as UTC and shift the day
		const dueDate = parseISO(prospect.followUpDate);
		const daysOverdue = differenceInCalendarDays(now, dueDate);
		return { status: statusOf(daysOverdue), manual: true, daysWaiting: 0, daysOverdue, dueDate };
	}
	if (!prospect.followUpSince) return NONE;
	const daysWaiting = daysSince(prospect.followUpSince, now);
	const daysOverdue = daysWaiting - delayDays;
	const dueDate = addDays(new Date(prospect.followUpSince), delayDays);
	return { status: statusOf(daysOverdue), manual: false, daysWaiting, daysOverdue, dueDate };
};
