import { Ref } from 'vue';

import { markAllAsRead$, markAsRead$, markAsUnread$ } from '~/api';

import { useTrigger } from './useTrigger';

export function useNotifications(): {
	markAsRead: (userId: string, notificationId: string) => void;
	markAsUnread: (userId: string, notificationId: string) => void;
	markAllAsRead: (userId: string) => void;
	isMarkingAsRead: Ref<boolean>;
	isMarkingAsUnread: Ref<boolean>;
	isMarkingAllAsRead: Ref<boolean>;
} {
	const { trigger: triggerMarkAsRead, loading: isMarkingAsRead } = useTrigger();
	const { trigger: triggerMarkAllAsRead, loading: isMarkingAllAsRead } = useTrigger();
	const { trigger: triggerMarkAsUnread, loading: isMarkingAsUnread } = useTrigger();
	const markAsRead = (userId: string, notificationId: string) => {
		triggerMarkAsRead(markAsRead$(userId, notificationId));
	};

	const markAsUnread = (userId: string, notificationId: string) => {
		triggerMarkAsUnread(markAsUnread$(userId, notificationId));
	};

	const markAllAsRead = (userId: string) => {
		triggerMarkAllAsRead(markAllAsRead$(userId));
	};

	return {
		markAsRead,
		markAsUnread,
		markAllAsRead,
		isMarkingAsRead,
		isMarkingAsUnread,
		isMarkingAllAsRead
	};
}
