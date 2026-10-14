import type { ProspectMessageDto } from '~/api/dtos/prospect-message.dto';

/** When the exchange happened. Drafts have no date yet: they fall back to when they were written. */
export function occurredAtOf(message: ProspectMessageDto): string {
	return message.sentAt ?? message.receivedAt ?? message.creationDate;
}

/** Chat order, oldest first, same as the backend. A message without a valid date goes last. */
export function sortByOccurrence(messages: ProspectMessageDto[]): ProspectMessageDto[] {
	const time = (message: ProspectMessageDto) => {
		const value = new Date(occurredAtOf(message)).getTime();
		return Number.isNaN(value) ? Number.POSITIVE_INFINITY : value;
	};
	return [...messages].sort((a, b) => time(a) - time(b));
}
