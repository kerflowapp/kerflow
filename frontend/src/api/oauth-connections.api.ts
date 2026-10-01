import type { AxiosObservable } from 'axios-observable';

import type { OAuthConnectionDto } from '~/api/dtos/oauth-connection.dto';
import { useHttp } from '~/composables/useHttp';

const { axiosInstance } = useHttp();

export function getOAuthConnections$(): AxiosObservable<OAuthConnectionDto[]> {
	return axiosInstance.get('/v1/users/me/oauth-connections');
}

export function revokeOAuthConnection$(clientId: string): AxiosObservable<void> {
	return axiosInstance.delete(`/v1/users/me/oauth-connections/${clientId}`);
}
