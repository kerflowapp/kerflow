import { ComputedRef, computed } from 'vue';

import { useAuthStore } from '~/stores';

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
