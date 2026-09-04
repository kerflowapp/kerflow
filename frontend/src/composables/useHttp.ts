import { Axios } from 'axios-observable';

import { getAuthToken } from '~/utils/tokenStorage';
import { getBackendUrl } from '~/utils/urlUtils';

const baseURL = getBackendUrl();

// Instance authentifiée (avec token)
const authenticatedAxiosInstance = Axios.create({
	baseURL,
	withCredentials: true,
	headers: {
		Accept: 'application/json',
		'Content-Type': 'application/json'
	}
});

authenticatedAxiosInstance.interceptors.request.use(
	async config => {
		const token = await getAuthToken();
		if (token) {
			config.headers.Authorization = `Bearer ${token}`;
		}
		return config;
	},
	error => {
		return Promise.reject(error);
	}
);

// Instance publique (sans token)
const publicAxiosInstance = Axios.create({
	baseURL,
	withCredentials: false,
	headers: {
		Accept: 'application/json',
		'Content-Type': 'application/json'
	}
});

const multipartConfig = {
	headers: {
		'Content-Type': 'multipart/form-data'
	},
	withCredentials: true
};

export function useHttp() {
	return {
		axiosInstance: authenticatedAxiosInstance,
		baseURL,
		multipartConfig
	};
}

export function usePublicHttp() {
	return {
		axiosInstance: publicAxiosInstance,
		baseURL
	};
}
