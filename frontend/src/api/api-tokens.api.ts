import type { AxiosObservable } from 'axios-observable';

import type { ApiTokenDto, CreatedApiTokenDto } from '~/api/dtos/api-token.dto';
import { useHttp } from '~/composables/useHttp';

const { axiosInstance } = useHttp();

export function getApiTokens$(): AxiosObservable<ApiTokenDto[]> {
	return axiosInstance.get('/v1/users/me/api-tokens');
}

export function createApiToken$(name: string): AxiosObservable<CreatedApiTokenDto> {
	return axiosInstance.post('/v1/users/me/api-tokens', { name });
}

export function revokeApiToken$(id: string): AxiosObservable<void> {
	return axiosInstance.delete(`/v1/users/me/api-tokens/${id}`);
}
