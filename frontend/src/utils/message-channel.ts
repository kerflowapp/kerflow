import type { MessageChannel } from '~/api/dtos/prospect-message.dto';

export const MESSAGE_CHANNELS: MessageChannel[] = ['EMAIL', 'PHONE', 'SMS', 'LINKEDIN', 'MEETING', 'OTHER'];

const CHANNEL_ICONS: Record<MessageChannel, string> = {
	EMAIL: 'mdi-email-outline',
	PHONE: 'mdi-phone-outline',
	SMS: 'mdi-message-text-outline',
	LINKEDIN: 'mdi-linkedin',
	MEETING: 'mdi-calendar-account-outline',
	OTHER: 'mdi-comment-outline'
};

/** Messages recorded before channels existed have none: they were all emails. */
export function channelOf(channel?: MessageChannel | null): MessageChannel {
	return channel ?? 'EMAIL';
}

export function channelIcon(channel?: MessageChannel | null): string {
	return CHANNEL_ICONS[channelOf(channel)];
}

export function channelLabelKey(channel?: MessageChannel | null): string {
	return `messages.channel.${channelOf(channel).toLowerCase()}`;
}
