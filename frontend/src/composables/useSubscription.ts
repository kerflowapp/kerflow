import type { Ref } from 'vue';
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { toast } from 'vuetify-sonner';

import type { BillingInterval } from '~/api';
import { createCheckoutSession$, createPortalSession$, syncCheckoutSession$ } from '~/api';
import type { SubscriptionDto } from '~/api/dtos/users.dto';
import { useAuthStore } from '~/stores';

import { useTrigger } from './useTrigger';

const DAY_IN_MS = 24 * 60 * 60 * 1000;

export function useSubscription(): {
	subscription: Ref<SubscriptionDto | undefined>;
	hasAccess: Ref<boolean>;
	isTrialExpired: Ref<boolean>;
	isTrialing: Ref<boolean>;
	isSubscribed: Ref<boolean>;
	isPastDue: Ref<boolean>;
	trialDaysRemaining: Ref<number>;
	isStartingCheckout: Ref<boolean>;
	isSyncingCheckout: Ref<boolean>;
	isOpeningPortal: Ref<boolean>;
	startCheckout: (interval?: BillingInterval) => void;
	syncCheckout: (sessionId: string, callbacks?: { onSuccess?: () => void; onError?: () => void }) => void;
	openPortal: () => void;
} {
	const { t } = useI18n();
	const authStore = useAuthStore();

	const subscription = computed(() => authStore.user?.subscription);
	const hasAccess = computed(() => subscription.value?.hasAccess ?? true);
	const isTrialExpired = computed(() => !hasAccess.value);
	const isTrialing = computed(() => subscription.value?.status === 'TRIALING');
	const isSubscribed = computed(() => subscription.value?.status === 'ACTIVE');
	const isPastDue = computed(() => subscription.value?.status === 'PAST_DUE');

	const trialDaysRemaining = computed(() => {
		const trialEndsAt = subscription.value?.trialEndsAt;
		if (!trialEndsAt) {
			return 0;
		}
		return Math.max(0, Math.ceil((new Date(trialEndsAt).getTime() - Date.now()) / DAY_IN_MS));
	});

	const { trigger: triggerCheckout, loading: isStartingCheckout } = useTrigger();
	const { trigger: triggerSync, loading: isSyncingCheckout } = useTrigger();
	const { trigger: triggerPortal, loading: isOpeningPortal } = useTrigger();

	const startCheckout = (interval: BillingInterval = 'MONTHLY') => {
		triggerCheckout(createCheckoutSession$(interval), {
			onSuccess: response => {
				window.location.href = response.data.url;
			},
			onError: () => {
				toast.error(t('billing.errors.checkout-failed'));
			}
		});
	};

	const syncCheckout = (sessionId: string, callbacks?: { onSuccess?: () => void; onError?: () => void }) => {
		triggerSync(syncCheckoutSession$(sessionId), {
			onSuccess: () => {
				authStore.refreshUserInformation().subscribe({
					next: () => callbacks?.onSuccess?.(),
					error: () => callbacks?.onError?.()
				});
			},
			onError: () => {
				toast.error(t('billing.errors.sync-failed'));
				callbacks?.onError?.();
			}
		});
	};

	const openPortal = () => {
		triggerPortal(createPortalSession$(), {
			onSuccess: response => {
				window.location.href = response.data.url;
			},
			onError: () => {
				toast.error(t('billing.errors.portal-failed'));
			}
		});
	};

	return {
		subscription,
		hasAccess,
		isTrialExpired,
		isTrialing,
		isSubscribed,
		isPastDue,
		trialDaysRemaining,
		isStartingCheckout,
		isSyncingCheckout,
		isOpeningPortal,
		startCheckout,
		syncCheckout,
		openPortal
	};
}
