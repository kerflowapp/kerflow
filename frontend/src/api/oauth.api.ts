import type { AxiosObservable } from 'axios-observable';

import { useHttp } from '~/composables';

import type { AuthorizationRequestDto, ConsentDecisionDto } from './dtos/oauth.dto';

const { axiosInstance } = useHttp();

const BASE_PATH = '/v1/oauth2/authorization-requests';

export function getAuthorizationRequest$(requestId: string): AxiosObservable<AuthorizationRequestDto> {
	return axiosInstance.get(`${BASE_PATH}/${requestId}`);
}

export function approveAuthorizationRequest$(requestId: string): AxiosObservable<ConsentDecisionDto> {
	return axiosInstance.post(`${BASE_PATH}/${requestId}/approve`);
}

export function denyAuthorizationRequest$(requestId: string): AxiosObservable<ConsentDecisionDto> {
	return axiosInstance.post(`${BASE_PATH}/${requestId}/deny`);
}
