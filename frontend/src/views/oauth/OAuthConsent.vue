<template>
	<v-container class="fill-height" fluid>
		<v-row align="center" justify="center">
			<v-col cols="12" lg="4" md="6" sm="8">
				<v-card elevation="0" variant="outlined">
					<v-card-text v-if="isFetching" class="py-12 text-center">
						<v-progress-circular color="primary" indeterminate />
					</v-card-text>

					<v-empty-state
						v-else-if="isExpired"
						icon="mdi-link-variant-off"
						:text="t('oauth.consent.expired-text')"
						:title="t('oauth.consent.expired-title')"
					>
						<template #actions>
							<v-btn color="primary" to="/dashboard" variant="text">
								{{ t('oauth.consent.back-to-app') }}
							</v-btn>
						</template>
					</v-empty-state>

					<template v-else-if="request">
						<v-card-item class="pt-6 text-center">
							<v-icon class="mb-2" color="primary" icon="mdi-robot-outline" size="48" />
							<v-card-title class="text-wrap">
								{{ t('oauth.consent.title', { clientName: request.clientName }) }}
							</v-card-title>
							<v-card-subtitle v-if="userEmail" class="text-wrap">
								{{ t('oauth.consent.connected-as', { email: userEmail }) }}
							</v-card-subtitle>
						</v-card-item>

						<v-card-text>
							<p class="text-body-2 text-medium-emphasis mb-4">
								{{ t('oauth.consent.description', { clientName: request.clientName }) }}
							</p>

							<v-list class="bg-transparent" density="compact">
								<v-list-item v-for="scope in request.scopes" :key="scope" class="px-0">
									<template #prepend>
										<v-icon class="mr-2" color="success" icon="mdi-check-circle-outline" size="small" />
									</template>
									<v-list-item-title class="text-body-2">
										{{ scopeLabel(scope) }}
									</v-list-item-title>
								</v-list-item>
							</v-list>

							<v-alert class="mt-4" density="compact" type="info" variant="tonal">
								{{ t('oauth.consent.revoke-hint') }}
							</v-alert>
						</v-card-text>

						<v-card-actions class="px-4 pb-4">
							<v-btn :disabled="isDeciding" variant="text" @click="deny(requestId)">
								{{ t('oauth.consent.deny') }}
							</v-btn>
							<v-spacer />
							<v-btn color="primary" :loading="isDeciding" variant="flat" @click="approve(requestId)">
								{{ t('oauth.consent.approve') }}
							</v-btn>
						</v-card-actions>
					</template>
				</v-card>
			</v-col>
		</v-row>
	</v-container>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute } from 'vue-router';

import { useOAuthConsent } from '~/composables';
import { useAuthStore } from '~/stores';

const { t, te } = useI18n();
const route = useRoute();
const authStore = useAuthStore();
const { request, isExpired, isFetching, isDeciding, fetchRequest, approve, deny } = useOAuthConsent();

const requestId = computed(() => String(route.query.request_id ?? ''));
const userEmail = computed(() => authStore.user?.email ?? authStore.user?.login ?? '');

// Scopes are server-defined; fall back to the raw value rather than showing a missing key.
const scopeLabel = (scope: string): string => {
	const key = `oauth.consent.scopes.${scope}`;
	return te(key) ? t(key) : scope;
};

onMounted(() => {
	if (!requestId.value) {
		isExpired.value = true;
		return;
	}
	fetchRequest(requestId.value);
});
</script>
