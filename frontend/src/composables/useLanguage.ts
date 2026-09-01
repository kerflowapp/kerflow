import { ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';

import { AVAILABLE_LOCALES, type AppLocale } from '~/plugin/i18n';

export function useLanguage() {
	const { locale } = useI18n();
	const availableLocales = AVAILABLE_LOCALES;
	const localeNames: Record<AppLocale, string> = {
		en: 'English',
		fr: 'Français'
	};

	// Keep track of current locale
	const currentLocale = ref(locale.value);

	// Change language and save to localStorage
	const setLocale = (newLocale: string) => {
		if ((availableLocales as readonly string[]).includes(newLocale)) {
			locale.value = newLocale;
			localStorage.setItem('locale', newLocale);
			currentLocale.value = newLocale;
		}
	};

	// Initial setup - sync with locale from i18n
	watch(locale, newValue => {
		currentLocale.value = newValue;
	});

	return {
		currentLocale,
		availableLocales,
		localeNames,
		setLocale
	};
}
