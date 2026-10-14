import type { AxiosObservable } from 'axios-observable';

import type { CreateProspectMessageDto, ProspectMessageDto, UpdateProspectMessageDto } from '~/api/dtos/prospect-message.dto';
import { useHttp } from '~/composables/useHttp';

const { axiosInstance } = useHttp();

export function getProspectMessages$(prospectId: string): AxiosObservable<ProspectMessageDto[]> {
	return axiosInstance.get(`/v1/prospects/${prospectId}/messages`);
}

export function createProspectMessage$(prospectId: string, data: CreateProspectMessageDto): AxiosObservable<ProspectMessageDto> {
	return axiosInstance.post(`/v1/prospects/${prospectId}/messages`, data);
}

export function updateProspectMessage$(
	prospectId: string,
	messageId: string,
	data: UpdateProspectMessageDto
): AxiosObservable<ProspectMessageDto> {
	return axiosInstance.put(`/v1/prospects/${prospectId}/messages/${messageId}`, data);
}

export function markProspectMessageSent$(prospectId: string, messageId: string): AxiosObservable<ProspectMessageDto> {
	return axiosInstance.patch(`/v1/prospects/${prospectId}/messages/${messageId}/sent`);
}

export function deleteProspectMessage$(prospectId: string, messageId: string): AxiosObservable<void> {
	return axiosInstance.delete(`/v1/prospects/${prospectId}/messages/${messageId}`);
}
