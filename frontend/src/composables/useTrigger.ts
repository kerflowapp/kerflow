import type { Observable, Subscription } from 'rxjs';
import { EMPTY, from } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { onUnmounted, ref } from 'vue';

import { refreshToken$ } from '~/api/auth.api';
import { useAuthStore } from '~/stores/auth.store';
import { getRefreshToken, setAuthToken, setRefreshToken } from '~/utils/tokenStorage';

let refreshInProgress: Promise<boolean> | null = null;

function attemptRefresh(): Promise<boolean> {
	if (refreshInProgress) {
		return refreshInProgress;
	}

	refreshInProgress = new Promise<boolean>(resolve => {
		from(getRefreshToken())
			.pipe(
				switchMap(refreshTokenValue => {
					if (!refreshTokenValue) {
						return EMPTY;
					}
					return refreshToken$(refreshTokenValue);
				}),
				switchMap(response =>
					from(
						(async () => {
							await setAuthToken(response.data.token);
							if (response.data.refreshToken) {
								await setRefreshToken(response.data.refreshToken);
							}
						})()
					)
				),
				catchError(() => {
					resolve(false);
					return EMPTY;
				})
			)
			.subscribe({
				next: () => resolve(true),
				complete: () => {
					// If EMPTY was returned (no refresh token), resolve false
					resolve(false);
				}
			});
	}).finally(() => {
		refreshInProgress = null;
	});

	return refreshInProgress;
}

export function useTrigger() {
	const loading = ref(false);
	const error = ref<Error | null>(null);
	const data = ref<unknown>(null);
	let subscription: Subscription | null = null;

	const trigger = <T>(
		observable: Observable<T>,
		{
			onSuccess,
			onError,
			onFinally
		}: {
			onSuccess?: (value: T) => void;
			onError?: (error: Error) => void;
			onFinally?: () => void;
		} = {}
	) => {
		// Clear previous error
		error.value = null;
		// Set loading state
		loading.value = true;

		// Unsubscribe from previous subscription if exists
		if (subscription) {
			subscription.unsubscribe();
		}

		const wrappedObservable = observable.pipe(
			catchError((err: Error) => {
				const axiosError = err as unknown as Record<string, unknown>;
				const response = axiosError.response as Record<string, unknown> | undefined;
				const isUnauthorized = response?.status === 401;

				if (!isUnauthorized) {
					throw err;
				}

				return from(attemptRefresh()).pipe(
					switchMap(refreshed => {
						if (refreshed) {
							// Retry the original observable with the new token
							return observable;
						}

						// No refresh token or refresh failed
						const authStore = useAuthStore();
						authStore.logout();
						return EMPTY;
					})
				);
			})
		);

		subscription = wrappedObservable.subscribe({
			next: (value: T) => {
				data.value = value;
				if (onSuccess) {
					onSuccess(value);
				}
			},
			error: (err: Error) => {
				error.value = err;
				loading.value = false;

				if (onError) {
					onError(err);
				}
			},
			complete: () => {
				loading.value = false;
				if (onFinally) {
					onFinally();
				}
			}
		});

		return subscription;
	};

	// Cleanup subscription on component unmount
	onUnmounted(() => {
		if (subscription) {
			subscription.unsubscribe();
		}
	});

	return {
		loading,
		error,
		data,
		trigger
	};
}
