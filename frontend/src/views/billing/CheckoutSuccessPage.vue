<template>
	<v-row align="center" class="fill-height" justify="center">
		<v-col cols="12" lg="5" md="6" sm="8">
			<v-card class="pa-6 text-center" elevation="0">
				<template v-if="isSyncingCheckout">
					<v-progress-circular class="mb-4" color="primary" indeterminate size="64" />
					<div class="text-h6">{{ t('billing.checkout.confirming') }}</div>
				</template>

				<template v-else-if="hasError">
					<v-icon class="mb-4" color="error" icon="mdi-alert-circle" size="64" />
					<div class="text-h6 mb-2">{{ t('billing.checkout.error-title') }}</div>
					<div class="text-body-2 text-grey mb-4">{{ t('billing.checkout.error-subtitle') }}</div>
					<v-btn color="primary" @click="sync">
						{{ t('billing.checkout.retry') }}
					</v-btn>
				</template>

				<template v-else>
					<v-icon class="mb-4" color="success" icon="mdi-check-circle" size="64" />
					<div class="text-h6">{{ t('billing.checkout.success-title') }}</div>
				</template>
			</v-card>
		</v-col>
	</v-row>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';

import { useSubscription } from '~/composables/useSubscription';

const { t } = useI18n();
const route = useRoute();
const router = useRouter();
const { syncCheckout, isSyncingCheckout } = useSubscription();

const hasError = ref(false);

const sync = () => {
	const sessionId = route.query.session_id as string | undefined;
	if (!sessionId) {
		router.replace('/dashboard');
		return;
	}

	hasError.value = false;
	syncCheckout(sessionId, {
		onSuccess: () => {
			setTimeout(() => router.replace('/dashboard'), 1500);
		},
		onError: () => {
			hasError.value = true;
		}
	});
};

onMounted(() => {
	sync();
});
</script>
