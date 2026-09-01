import { computed } from 'vue';
import { useRouter } from 'vue-router';

import { useAuthStore } from '../stores';

export function useAuth() {
	const authStore = useAuthStore();
	const router = useRouter();

	const isLoggedIn = computed(() => authStore.isLoggedIn);
	const user = computed(() => authStore.user);

	const login = (request: { username: string; password: string }) => {
		authStore.login(request);
	};

	const logout = () => {
		authStore.logout();
		router.push('/');
	};

	return {
		isLoggedIn,
		user,
		login,
		logout
	};
}
