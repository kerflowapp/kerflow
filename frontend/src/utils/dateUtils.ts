import { differenceInCalendarDays } from 'date-fns';

export const formatDate = (date: string): string => {
	if (!date) return '';
	return new Date(date).toLocaleDateString('fr-FR', { year: 'numeric', month: 'long', day: 'numeric' });
};

/** Calendar days elapsed since the given date: 0 the same day, 1 the next day, whatever the hour. */
export const daysSince = (date: string, now: Date = new Date()): number => differenceInCalendarDays(now, new Date(date));
