import { ComputedRef, computed } from 'vue';

import { useAuthStore } from '~/stores/auth.store';

export function useUserRole(): {
	isAuthenticated: ComputedRef<boolean>;
} {
	const authStore = useAuthStore();

	const isAuthenticated = computed(() => {
		return authStore.isLoggedIn;
	});

	return {
		isAuthenticated
	};
}
