import EmptyLayout from '@/layouts/EmptyLayout.vue';
import Auth from '@/views/Auth.vue';
import ResetPassword from '@/views/ResetPassword.vue';
import type { RouteRecordRaw } from 'vue-router';
import * as VueRouter from 'vue-router';
import { createWebHistory } from 'vue-router';

import { useBetaMode } from '~/composables/useBetaMode';
import { getSavedLocale, resolveBrowserLocale, setActiveLocale } from '~/plugin/i18n';
import { useAuthStore } from '~/stores/auth.store';

import MainRoutes from '../router/main-routes';

const routes = [
	{
		path: '/auth',
		meta: {
			requiresAuth: false
		},
		component: EmptyLayout,
		children: [
			{
				path: '/auth',
				component: Auth,
				alias: ['/signin', '/signup', '/verify']
			},
			{
				path: '/reset-password',
				component: ResetPassword
			}
		]
	},
	{
		path: '/:pathMatch(.*)*',
		component: () => import('@/views/404.vue')
	},
	MainRoutes
];

const router = VueRouter.createRouter({
	history: createWebHistory(),
	routes: routes as RouteRecordRaw[]
});

// /oauth: connecting an MCP client must work even when the subscription lapsed — access is
// enforced per tool call by the backend, and the paywall here would just dead-end the flow.
const PATHS_ALLOWED_WITHOUT_SUBSCRIPTION = ['/subscribe', '/prospects', '/settings', '/checkout/success', '/auth', '/oauth'];

// The sign-in screens belong to the visitor, not to whoever used this browser before: they follow
// the browser language and ignore the stored preference, which is restored once inside the app.
const UNAUTHENTICATED_PATHS = ['/auth', '/signin', '/signup', '/verify', '/reset-password'];

function applyLocale(toPath: string): void {
	setActiveLocale(UNAUTHENTICATED_PATHS.includes(toPath) ? resolveBrowserLocale() : (getSavedLocale() ?? resolveBrowserLocale()));
}

function isAllowedWithoutSubscription(path: string): boolean {
	return PATHS_ALLOWED_WITHOUT_SUBSCRIPTION.some(allowed => path === allowed || path.startsWith(`${allowed}/`));
}

function resolvePaywall(toPath: string): string | undefined {
	const authStore = useAuthStore();
	const hasAccess = authStore.user?.subscription?.hasAccess ?? true;

	if (!hasAccess && !isAllowedWithoutSubscription(toPath)) {
		return '/prospects';
	}
	if (hasAccess && toPath === '/subscribe') {
		return '/dashboard';
	}
	return undefined;
}

router.beforeEach((to, from, next) => {
	const { isBetaEnabled } = useBetaMode();
	const authStore = useAuthStore();

	applyLocale(to.path);

	if (!isBetaEnabled.value && to.meta.betaOnly) {
		return next('/dashboard');
	}

	// Root path: no public homepage anymore
	if (to.path === '/') {
		return next(authStore.isLoggedIn ? '/dashboard' : '/auth');
	}

	const requiresAuth = to.matched.some(record => record.meta.requiresAuth);

	if (requiresAuth && !authStore.isLoggedIn) {
		// Keep the destination: a logged-out user landing on the OAuth consent screen must
		// come back to it after signing in, or the connector flow silently dies on /dashboard.
		return next({ path: '/auth', query: { redirect: to.fullPath } });
	}

	if (authStore.isLoggedIn) {
		if (!authStore.user) {
			authStore.refreshUserInformation().subscribe({
				next: () => {
					next(resolvePaywall(to.path));
				},
				error: () => {
					authStore.logout();
				}
			});
			return;
		}
	}

	if (authStore.isLoggedIn && authStore.user) {
		authStore.refreshUserInformation().subscribe({
			error: () => {
				authStore.logout();
			}
		});
		return next(resolvePaywall(to.path));
	}

	next();
});

export default router;
