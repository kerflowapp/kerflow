export type SubscriptionStatus = 'TRIALING' | 'ACTIVE' | 'PAST_DUE' | 'CANCELLED' | 'INACTIVE';

export type SubscriptionDto = {
	status: SubscriptionStatus;
	interval: 'MONTHLY' | 'ANNUAL' | null;
	trialEndsAt: string | null;
	currentPeriodEnd: string | null;
	cancelledAt: string | null;
	hasAccess: boolean;
};

export type UserDto = {
	id: string;
	login: string;
	email: string;
	firstName: string;
	lastName: string;
	phoneNumber: string;
	city: string;
	profession: string;
	/** Days without reply before a prospect is flagged for follow-up; the backend applies the default */
	followUpDelayDays: number;
	notifications: NotificationDto[];
	subscription?: SubscriptionDto;
};

export type NotificationDto = {
	id: string;
	content: string;
	creationDate: string;
	type: string;
	title: string;
	isRead: boolean;
};
