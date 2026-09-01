import type { AxiosObservable } from 'axios-observable';

import type { SubscriptionDto } from '~/api/dtos';
import { useHttp } from '~/composables';

const { axiosInstance } = useHttp();

export type BillingInterval = 'MONTHLY' | 'ANNUAL';

export type CheckoutSessionDto = {
	url: string;
};

export type PortalSessionDto = {
	url: string;
};

export function createCheckoutSession$(interval: BillingInterval = 'MONTHLY'): AxiosObservable<CheckoutSessionDto> {
	return axiosInstance.post('/v1/billing/checkout', { interval });
}

export function syncCheckoutSession$(sessionId: string): AxiosObservable<SubscriptionDto> {
	return axiosInstance.post('/v1/billing/checkout/sync', { sessionId });
}

export function createPortalSession$(): AxiosObservable<PortalSessionDto> {
	return axiosInstance.post('/v1/billing/portal');
}
