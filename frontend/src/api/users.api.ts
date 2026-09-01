import type { AxiosObservable } from 'axios-observable';

import { useHttp } from '~/composables';

const { axiosInstance } = useHttp();

export function updateUser$(userId: string, firstName: string, lastName: string, phoneNumber: string): AxiosObservable<void> {
	return axiosInstance.post(`/v1/users/${userId}`, { firstName, lastName, phoneNumber });
}
