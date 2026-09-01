import posthog from 'posthog-js';
import type { App, Plugin } from 'vue';

const DEFAULT_API_HOST = 'https://eu.i.posthog.com';

/**
 * PostHog analytics. Disabled unless both VITE_POSTHOG_ENABLED is 'true' and
 * VITE_POSTHOG_KEY holds your own project key: a self-hosted instance should
 * never report into someone else's project.
 */
const posthogPlugin: Plugin = {
	install(app: App) {
		const apiKey = import.meta.env.VITE_POSTHOG_KEY;
		if (!apiKey) {
			console.warn('[posthog] VITE_POSTHOG_KEY is not set, analytics are disabled');
			return;
		}

		app.config.globalProperties.$posthog = posthog.init(apiKey, {
			api_host: import.meta.env.VITE_POSTHOG_HOST || DEFAULT_API_HOST,
			defaults: '2025-05-24'
		});
	}
};

export default posthogPlugin;
