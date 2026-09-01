import type { AxiosObservable } from 'axios-observable';

import type { CreateProspectMessageDto, ProspectMessageDto } from '~/api/dtos';
import { useHttp } from '~/composables';

export function getProspectMessages$(prospectId: string): AxiosObservable<ProspectMessageDto[]> {
	const { axiosInstance } = useHttp();
	return axiosInstance.get(`/v1/prospects/${prospectId}/messages`);
}

export function createProspectMessage$(prospectId: string, data: CreateProspectMessageDto): AxiosObservable<ProspectMessageDto> {
	const { axiosInstance } = useHttp();
	return axiosInstance.post(`/v1/prospects/${prospectId}/messages`, data);
}

export function markProspectMessageSent$(prospectId: string, messageId: string): AxiosObservable<ProspectMessageDto> {
	const { axiosInstance } = useHttp();
	return axiosInstance.patch(`/v1/prospects/${prospectId}/messages/${messageId}/sent`);
}

export function deleteProspectMessage$(prospectId: string, messageId: string): AxiosObservable<void> {
	const { axiosInstance } = useHttp();
	return axiosInstance.delete(`/v1/prospects/${prospectId}/messages/${messageId}`);
}
