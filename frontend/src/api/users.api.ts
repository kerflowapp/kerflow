import type { AxiosObservable } from 'axios-observable';

import { useHttp } from '~/composables/useHttp';

const { axiosInstance } = useHttp();

export function updateUser$(userId: string, firstName: string, lastName: string, phoneNumber: string): AxiosObservable<void> {
	return axiosInstance.post(`/v1/users/${userId}`, { firstName, lastName, phoneNumber });
}

export function updatePreferences$(followUpDelayDays: number): AxiosObservable<void> {
	return axiosInstance.put('/v1/users/me/preferences', { followUpDelayDays });
}
