import { createI18n } from 'vue-i18n';

import en from '~/locales/en.json';
import fr from '~/locales/fr.json';

export const AVAILABLE_LOCALES = ['fr', 'en'] as const;

export type AppLocale = (typeof AVAILABLE_LOCALES)[number];

const FALLBACK_LOCALE: AppLocale = 'fr';
const STORAGE_KEY = 'locale';

function isAppLocale(value: string | null | undefined): value is AppLocale {
	return !!value && (AVAILABLE_LOCALES as readonly string[]).includes(value);
}

/**
 * First browser language we actually translate, falling back to French. `navigator.languages`
 * is ordered by preference, so a `de, en, fr` browser gets English rather than the default.
 */
export function resolveBrowserLocale(): AppLocale {
	const candidates = navigator.languages?.length ? navigator.languages : [navigator.language];

	for (const candidate of candidates) {
		const language = candidate?.split('-')[0]?.toLowerCase();
		if (isAppLocale(language)) {
			return language;
		}
	}

	return FALLBACK_LOCALE;
}

/** Explicit choice made through the language selector, or null when there is none. */
export function getSavedLocale(): AppLocale | null {
	const saved = localStorage.getItem(STORAGE_KEY);
	return isAppLocale(saved) ? saved : null;
}

const i18n = createI18n({
	locale: getSavedLocale() ?? resolveBrowserLocale(),
	fallbackLocale: FALLBACK_LOCALE,
	legacy: false,
	messages: {
		fr,
		en
	}
});

/** Switches the displayed language without touching the stored preference. */
export function setActiveLocale(value: AppLocale): void {
	i18n.global.locale.value = value;
}

export default i18n;
