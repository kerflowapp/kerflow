<template>
	<div>
		<h3 class="text-h5 font-weight-bold mb-6">{{ t('settings.billing.title') }}</h3>

		<v-alert v-if="wasCheckoutCancelled" class="mb-4" closable type="info" variant="tonal">
			{{ t('settings.billing.checkout-cancelled') }}
		</v-alert>

		<v-card variant="outlined">
			<v-card-title class="text-subtitle-1 font-weight-bold py-3 px-4 bg-grey-lighten-4">
				{{ t('settings.billing.current-plan') }}
			</v-card-title>
			<v-card-text class="pa-4">
				<div class="d-flex align-center mb-4">
					<div class="flex-grow-1">
						<div class="text-subtitle-1 font-weight-medium">{{ t('settings.billing.plan-name') }}</div>
						<div class="text-caption text-grey">{{ displayPrice }}{{ displayPer }}</div>
					</div>
					<v-chip :color="statusColor" variant="tonal">
						{{ t(`settings.billing.status.${statusKey}`) }}
					</v-chip>
				</div>

				<div v-if="isTrialing" class="text-body-2 mb-4">
					{{ t('settings.billing.trial-remaining', { count: trialDaysRemaining }, trialDaysRemaining) }}
				</div>
				<div v-else-if="isSubscribed && currentPeriodEnd" class="text-body-2 mb-4">
					{{ t('settings.billing.renews-on', { date: currentPeriodEnd }) }}
				</div>

				<v-btn-toggle
					v-if="!isSubscribed && !isPastDue"
					v-model="interval"
					class="d-flex mb-4"
					color="primary"
					divided
					mandatory
					variant="outlined"
				>
					<v-btn class="flex-grow-1" value="MONTHLY">
						{{ t('billing.paywall.interval-monthly') }}
						<span class="text-caption text-grey ml-1">
							{{ t('billing.paywall.price') }}{{ t('billing.paywall.per-month') }}
						</span>
					</v-btn>
					<v-btn class="flex-grow-1" value="ANNUAL">
						{{ t('billing.paywall.interval-annual') }}
						<span class="text-caption text-grey ml-1">
							{{ t('billing.paywall.price-annual') }}{{ t('billing.paywall.per-year') }}
						</span>
						<v-chip class="ml-2" color="success" label size="x-small">
							{{ t('billing.paywall.savings-badge') }}
						</v-chip>
					</v-btn>
				</v-btn-toggle>

				<div class="d-flex justify-end">
					<v-btn
						v-if="!isSubscribed && !isPastDue"
						color="primary"
						:loading="isStartingCheckout"
						@click="startCheckout(interval)"
					>
						{{ t('billing.paywall.subscribe') }}
					</v-btn>
					<v-btn v-else color="primary" :loading="isOpeningPortal" variant="outlined" @click="openPortal">
						{{ t('settings.billing.manage-subscription') }}
					</v-btn>
				</div>
			</v-card-text>
		</v-card>
	</div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute } from 'vue-router';

import type { BillingInterval } from '~/api/billing.api';
import { useSubscription } from '~/composables';

const { t, locale } = useI18n();
const route = useRoute();
const {
	subscription,
	isTrialing,
	isSubscribed,
	isPastDue,
	trialDaysRemaining,
	startCheckout,
	openPortal,
	isStartingCheckout,
	isOpeningPortal
} = useSubscription();

const interval = ref<BillingInterval>('ANNUAL');

const activeInterval = computed<BillingInterval>(() => (isSubscribed.value ? (subscription.value?.interval ?? 'MONTHLY') : interval.value));
const displayPrice = computed(() => (activeInterval.value === 'ANNUAL' ? t('billing.paywall.price-annual') : t('billing.paywall.price')));
const displayPer = computed(() => (activeInterval.value === 'ANNUAL' ? t('billing.paywall.per-year') : t('billing.paywall.per-month')));

const wasCheckoutCancelled = computed(() => route.query.checkout === 'cancelled');

const statusKey = computed(() => (subscription.value?.status ?? 'INACTIVE').toLowerCase().replace('_', '-'));

const statusColor = computed(() => {
	switch (subscription.value?.status) {
		case 'ACTIVE':
			return 'success';
		case 'TRIALING':
			return 'info';
		case 'PAST_DUE':
			return 'warning';
		default:
			return 'error';
	}
});

const currentPeriodEnd = computed(() => {
	const periodEnd = subscription.value?.currentPeriodEnd;
	return periodEnd ? new Date(periodEnd).toLocaleDateString(locale.value) : null;
});
</script>
