<template>
	<v-row align="center" class="fill-height" justify="center">
		<v-col cols="12" lg="5" md="6" sm="8">
			<v-card class="pa-4" elevation="0">
				<v-card-title class="text-h5 font-weight-bold text-center">
					{{ t('billing.paywall.title') }}
				</v-card-title>
				<v-card-subtitle class="text-center text-wrap">
					{{ t('billing.paywall.subtitle') }}
				</v-card-subtitle>
				<v-card-text>
					<v-btn-toggle v-model="interval" class="d-flex mt-4" color="primary" divided mandatory variant="outlined">
						<v-btn class="flex-grow-1" value="MONTHLY">{{ t('billing.paywall.interval-monthly') }}</v-btn>
						<v-btn class="flex-grow-1" value="ANNUAL">
							{{ t('billing.paywall.interval-annual') }}
							<v-chip class="ml-2" color="success" label size="x-small">
								{{ t('billing.paywall.savings-badge') }}
							</v-chip>
						</v-btn>
					</v-btn-toggle>

					<div class="text-center my-6">
						<span class="text-h3 font-weight-bold">
							{{ interval === 'ANNUAL' ? t('billing.paywall.price-annual') : t('billing.paywall.price') }}
						</span>
						<span class="text-body-1 text-grey">
							{{ interval === 'ANNUAL' ? t('billing.paywall.per-year') : t('billing.paywall.per-month') }}
						</span>
					</div>

					<v-list class="bg-transparent" density="compact">
						<v-list-item v-for="feature in features" :key="feature" prepend-icon="mdi-check-circle">
							<v-list-item-title>{{ t(feature) }}</v-list-item-title>
						</v-list-item>
					</v-list>

					<v-btn block class="mt-6" color="primary" :loading="isStartingCheckout" size="large" @click="startCheckout(interval)">
						{{ t('billing.paywall.subscribe') }}
					</v-btn>

					<div class="d-flex justify-space-between mt-4">
						<v-btn size="small" to="/settings" variant="text">
							{{ t('navigation.settings') }}
						</v-btn>
						<v-btn color="error" size="small" variant="text" @click="logout">
							{{ t('common.logout') }}
						</v-btn>
					</div>
				</v-card-text>
			</v-card>
		</v-col>
	</v-row>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';

import type { BillingInterval } from '~/api/billing.api';
import { useAuth, useSubscription } from '~/composables';

const { t } = useI18n();
const { logout } = useAuth();
const { startCheckout, isStartingCheckout } = useSubscription();

const interval = ref<BillingInterval>('ANNUAL');

const features = [
	'billing.paywall.features.prospects',
	'billing.paywall.features.pipeline',
	'billing.paywall.features.search',
	'billing.paywall.features.cancel-anytime'
];
</script>
