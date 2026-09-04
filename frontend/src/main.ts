import DateFnsAdapter from '@date-io/date-fns';
import { enUS } from 'date-fns/locale/en-US';
import { fr as frFR } from 'date-fns/locale/fr';
import { createPinia } from 'pinia';
import { createApp } from 'vue';
import { createVuetify } from 'vuetify';
import * as components from 'vuetify/components';
import * as directives from 'vuetify/directives';
import { aliases, mdi } from 'vuetify/iconsets/mdi';
import { VFileUpload } from 'vuetify/labs/VFileUpload';
import * as labsComponents from 'vuetify/labs/components';
import 'vuetify/styles';

import App from './App.vue';
import i18n from './plugin/i18n';
import posthogPlugin from './plugin/posthog';
import router from './router';

const savedTheme = 'light';

const app = createApp(App);

const vuetify = createVuetify({
	defaults: {
		VBtn: {
			class: 'text-none'
		}
	},
	components: {
		...components,
		...labsComponents,
		VFileUpload
	},
	date: {
		adapter: DateFnsAdapter,
		locale: {
			en: enUS,
			fr: frFR
		}
	},
	directives,
	icons: {
		defaultSet: 'mdi',
		aliases,
		sets: {
			mdi
		}
	},
	theme: {
		defaultTheme: savedTheme,
		themes: {
			light: {
				colors: {
					primary: '#4F46E5',
					secondary: '#F59E0B',
					accent: '#EEF2FF',
					background: '#FAFAFA',
					surface: '#F8FAFC',
					success: '#10B981',
					info: '#3B82F6',
					warning: '#F59E0B',
					error: '#EF4444'
				}
			},
			dark: {
				colors: {
					primary: '#6366F1',
					secondary: '#FBBF24',
					accent: '#312E81',
					background: '#111111',
					surface: '#1A1A1A',
					success: '#10B981',
					info: '#3B82F6',
					warning: '#F59E0B',
					error: '#EF4444'
				}
			}
		}
	}
});

app.use(vuetify);
app.use(createPinia());
app.use(router);
if (import.meta.env.VITE_POSTHOG_ENABLED === 'true') {
	app.use(posthogPlugin);
}
app.use(i18n);

app.mount('#app');
