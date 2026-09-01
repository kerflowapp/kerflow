<template>
	<v-alert v-if="showBanner" class="mb-4" density="compact" :type="isPastDue ? 'warning' : 'info'" variant="tonal">
		<div class="d-flex align-center flex-wrap">
			<span class="flex-grow-1">
				{{
					isPastDue
						? t('billing.trial-banner.past-due')
						: t('billing.trial-banner.days-remaining', { count: trialDaysRemaining }, trialDaysRemaining)
				}}
			</span>
			<v-btn color="primary" size="small" to="/settings/billing">
				{{ t('billing.paywall.subscribe') }}
			</v-btn>
		</div>
	</v-alert>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

import { useSubscription } from '~/composables';

const { t } = useI18n();
const { isTrialing, isPastDue, trialDaysRemaining } = useSubscription();

const showBanner = computed(() => isTrialing.value || isPastDue.value);
</script>
