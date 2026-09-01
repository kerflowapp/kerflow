import type { AxiosObservable } from 'axios-observable';

import type { UserDto } from '~/api/dtos';
import { useHttp } from '~/composables';

import { CheckEmailResponse, SigninResponse, SignupRequest, UpdatePasswordRequest } from './dtos';

const { axiosInstance } = useHttp();

export function checkEmail$(email: string): AxiosObservable<CheckEmailResponse> {
	return axiosInstance.get('/v1/authentication/check-email', { params: { email } });
}

export function login$(request: { username: string; password: string; impersonateEmail?: string }): AxiosObservable<SigninResponse> {
	return axiosInstance.post('/v1/authentication/signin', request);
}

export function signup$(request: SignupRequest): AxiosObservable<any> {
	return axiosInstance.post('/v1/authentication/signup', request);
}

export function updatePassword$(request: UpdatePasswordRequest): AxiosObservable<any> {
	return axiosInstance.post('/v1/authentication/password', request);
}

export function getUserInformation$(): AxiosObservable<UserDto> {
	return axiosInstance.get('/v1/authentication/user-context');
}

export function verifyAccount$(request: { email: string; verificationCode: string }): AxiosObservable<unknown> {
	return axiosInstance.post('/v1/authentication/verify', request);
}

export function resendVerificationCode$(request: { email: string }): AxiosObservable<any> {
	return axiosInstance.post('/v1/authentication/resend-verification', request);
}

export function refreshToken$(refreshToken: string): AxiosObservable<SigninResponse> {
	return axiosInstance.post('/v1/authentication/refresh', { refreshToken });
}
