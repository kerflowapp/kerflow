import DynamicLayout from '@/layouts/DynamicLayout.vue';

import SettingsPage from '~/views/settings/SettingsPage.vue';

const MainRoutes = {
	path: '/',
	meta: {
		requiresAuth: false
	},
	component: DynamicLayout,
	children: [
		{
			path: '',
			redirect: '/auth',
			meta: { requiresAuth: false }
		},
		{
			path: '/dashboard',
			component: () => import('~/views/dashboard/Dashboard.vue'),
			meta: { requiresAuth: true }
		},
		{
			path: '/search',
			component: () => import('~/views/search/SearchPage.vue'),
			meta: { requiresAuth: true }
		},
		{
			path: '/prospects',
			component: () => import('~/views/prospects/ProspectsBoard.vue'),
			meta: { requiresAuth: true }
		},
		{
			path: '/subscribe',
			component: () => import('~/views/billing/PaywallPage.vue'),
			meta: { requiresAuth: true }
		},
		{
			// OAuth consent screen, reached by a redirect from the backend authorization
			// endpoint when an MCP client (Claude.ai) asks for access.
			path: '/oauth/authorize',
			component: () => import('~/views/oauth/OAuthConsent.vue'),
			meta: { requiresAuth: true }
		},
		{
			path: '/checkout/success',
			component: () => import('~/views/billing/CheckoutSuccessPage.vue'),
			meta: { requiresAuth: true }
		},
		{
			path: '/settings',
			component: SettingsPage,
			meta: { requiresAuth: true },
			children: [
				{
					path: '',
					redirect: '/settings/profile'
				},
				{
					path: 'profile',
					component: () => import('../views/settings/ProfileSettings.vue')
				},
				{
					path: 'security',
					component: () => import('../views/settings/SecuritySettings.vue')
				},
				{
					path: 'billing',
					component: () => import('../views/settings/BillingSettings.vue')
				},
				{
					path: 'api-tokens',
					component: () => import('../views/settings/ApiTokensSettings.vue')
				}
			]
		}
	]
};

export default MainRoutes;
