import type { Ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { toast } from '~/composables/useToast';

import { updatePreferences$ } from '~/api/users.api';
import { useAuthStore } from '~/stores/auth.store';

import { useTrigger } from './useTrigger';

export function usePreferences(): {
	isSaving: Ref<boolean>;
	saveFollowUpDelay: (followUpDelayDays: number) => void;
} {
	const { t } = useI18n();
	const authStore = useAuthStore();
	const { trigger, loading: isSaving } = useTrigger();

	const saveFollowUpDelay = (followUpDelayDays: number) => {
		trigger(updatePreferences$(followUpDelayDays), {
			onSuccess: () => {
				authStore.user = { ...authStore.user, followUpDelayDays };
				toast.success(t('success.preferences-updated'));
			},
			onError: () => {
				toast.error(t('errors.update-preferences-failed'));
			}
		});
	};

	return { isSaving, saveFollowUpDelay };
}
