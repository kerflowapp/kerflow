import type { AxiosObservable } from 'axios-observable';

import { useHttp } from '~/composables';

const { axiosInstance } = useHttp();

export function markAsRead$(userId: string, notificationId: string): AxiosObservable<void> {
	return axiosInstance.post(`/v1/users/${userId}/notifications/${notificationId}/read`);
}

export function markAsUnread$(userId: string, notificationId: string): AxiosObservable<void> {
	return axiosInstance.post(`/v1/users/${userId}/notifications/${notificationId}/unread`);
}

export function markAllAsRead$(userId: string): AxiosObservable<void> {
	return axiosInstance.put(`/v1/users/${userId}/notifications/read`);
}
