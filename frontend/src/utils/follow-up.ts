import { addDays } from 'date-fns';

import type { ProspectDto } from '~/api/dtos/prospect.dto';
import { daysSince } from '~/utils/dateUtils';

/** Mirrors FollowUpService.DEFAULT_DELAY_DAYS, used until the user context is loaded */
export const DEFAULT_FOLLOW_UP_DELAY_DAYS = 5;
export const MIN_FOLLOW_UP_DELAY_DAYS = 1;
export const MAX_FOLLOW_UP_DELAY_DAYS = 90;

export type FollowUpStatus = 'none' | 'upcoming' | 'soon' | 'due';

export type FollowUpState = {
	status: FollowUpStatus;
	daysWaiting: number;
	/** 0 on the day the follow-up becomes due, negative before */
	daysOverdue: number;
	/** Day the follow-up becomes due; null when no follow-up applies */
	dueDate: Date | null;
};

const NONE: FollowUpState = { status: 'none', daysWaiting: 0, daysOverdue: 0, dueDate: null };

/**
 * The backend decides whether a prospect is waiting for a reply (followUpSince); this only
 * compares that date with the user's delay. "soon" means due tomorrow, "upcoming" later on.
 */
export const followUpStateOf = (prospect: ProspectDto, delayDays: number, now: Date = new Date()): FollowUpState => {
	if (!prospect.followUpSince) return NONE;
	const daysWaiting = daysSince(prospect.followUpSince, now);
	const daysOverdue = daysWaiting - delayDays;
	const dueDate = addDays(new Date(prospect.followUpSince), delayDays);
	const status: FollowUpStatus = daysOverdue >= 0 ? 'due' : daysOverdue === -1 ? 'soon' : 'upcoming';
	return { status, daysWaiting, daysOverdue, dueDate };
};
