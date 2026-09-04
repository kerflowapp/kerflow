<template>
	<v-card elevation="0" variant="outlined">
		<v-card-title class="bg-grey-lighten-4 d-flex align-center">
			{{ t('settings.api-tokens.tokens-title') }}
			<v-spacer />
			<v-btn color="primary" prepend-icon="mdi-plus" size="small" variant="flat" @click="openCreateDialog">
				{{ t('settings.api-tokens.create-button') }}
			</v-btn>
		</v-card-title>
		<v-card-text class="pt-4">
			<p class="text-body-2 text-medium-emphasis mb-4">
				{{ t('settings.api-tokens.description') }}
			</p>

			<div v-if="isFetching" class="d-flex justify-center py-6">
				<v-progress-circular indeterminate />
			</div>

			<v-list v-else-if="tokens.length > 0" lines="two">
				<v-list-item v-for="token in tokens" :key="token.id">
					<template #prepend>
						<v-icon icon="mdi-key-outline" />
					</template>
					<v-list-item-title>
						{{ token.name }}
						<v-chip v-if="token.revokedAt" class="ml-2" color="grey" size="x-small">
							{{ t('settings.api-tokens.revoked') }}
						</v-chip>
					</v-list-item-title>
					<v-list-item-subtitle>
						<code>{{ token.tokenPrefix }}…</code>
						· {{ t('settings.api-tokens.created-on', { date: formatDate(token.creationDate) }) }} ·
						{{
							token.lastUsedAt
								? t('settings.api-tokens.last-used', { date: formatDate(token.lastUsedAt) })
								: t('settings.api-tokens.never-used')
						}}
					</v-list-item-subtitle>
					<template #append>
						<v-btn
							v-if="!token.revokedAt"
							:aria-label="t('settings.api-tokens.revoke-button')"
							color="error"
							icon="mdi-delete"
							size="small"
							variant="text"
							@click="openRevokeDialog(token)"
						/>
					</template>
				</v-list-item>
			</v-list>

			<v-empty-state
				v-else
				icon="mdi-key-outline"
				:text="t('settings.api-tokens.empty-text')"
				:title="t('settings.api-tokens.empty-title')"
			/>
		</v-card-text>

		<v-dialog v-model="createDialog" max-width="480">
			<v-card elevation="0">
				<v-card-title class="text-h6">{{ t('settings.api-tokens.create-dialog-title') }}</v-card-title>
				<v-card-text>
					<v-form ref="createForm" fast-fail @submit.prevent="handleCreate">
						<v-text-field v-model="tokenName" autofocus :label="t('settings.api-tokens.name-label')" :rules="nameRules" />
					</v-form>
				</v-card-text>
				<v-card-actions>
					<v-spacer />
					<v-btn variant="text" @click="createDialog = false">{{ t('common.cancel') }}</v-btn>
					<v-btn color="primary" :loading="isCreating" variant="flat" @click="handleCreate">
						{{ t('settings.api-tokens.create-button') }}
					</v-btn>
				</v-card-actions>
			</v-card>
		</v-dialog>

		<v-dialog v-model="createdDialog" max-width="560" persistent>
			<v-card elevation="0">
				<v-card-title class="text-h6">{{ t('settings.api-tokens.created-dialog-title') }}</v-card-title>
				<v-card-text>
					<v-alert class="mb-4" density="compact" type="warning" variant="tonal">
						{{ t('settings.api-tokens.one-time-warning') }}
					</v-alert>
					<div class="d-flex align-center token-box pa-3">
						<code class="token-value">{{ createdToken?.token }}</code>
						<v-btn icon="mdi-content-copy" size="small" variant="text" @click="copyToken" />
					</div>
				</v-card-text>
				<v-card-actions>
					<v-spacer />
					<v-btn variant="text" @click="closeCreatedDialog">{{ t('common.close') }}</v-btn>
				</v-card-actions>
			</v-card>
		</v-dialog>

		<confirmation-dialog v-model="revokeDialog" level="error" :title="t('settings.api-tokens.revoke-confirm-title')">
			<template #message>
				{{ t('settings.api-tokens.revoke-confirm-message', { name: tokenToRevoke?.name }) }}
			</template>
			<template #actions>
				<v-btn variant="text" @click="revokeDialog = false">{{ t('common.cancel') }}</v-btn>
				<v-btn color="error" :loading="isRevoking" variant="flat" @click="handleRevoke">
					{{ t('settings.api-tokens.revoke-button') }}
				</v-btn>
			</template>
		</confirmation-dialog>
	</v-card>

	<connect-claude-card class="mt-4" />
</template>

<style scoped>
.token-box {
	background-color: rgba(var(--v-theme-on-surface), 0.05);
	border-radius: 4px;
}

.token-value {
	flex: 1;
	word-break: break-all;
}
</style>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { toast } from '~/composables/useToast';

import type { ApiTokenDto, CreatedApiTokenDto } from '~/api/dtos/api-token.dto';
import ConfirmationDialog from '~/components/ConfirmationDialog.vue';
import { useApiTokens } from '~/composables/useApiTokens';

import ConnectClaudeCard from './ConnectClaudeCard.vue';

const { t } = useI18n();
const { tokens, isFetching, isCreating, isRevoking, fetchTokens, createToken, revokeToken } = useApiTokens();

const createDialog = ref(false);
const createdDialog = ref(false);
const createForm = ref<{ validate: () => Promise<{ valid: boolean }> } | null>(null);
const tokenName = ref('');
const createdToken = ref<CreatedApiTokenDto | null>(null);
const revokeDialog = ref(false);
const tokenToRevoke = ref<ApiTokenDto | null>(null);

const nameRules = [(value: string) => (value?.trim().length > 0 ? true : t('settings.api-tokens.name-required'))];

const formatDate = (value: string): string => new Date(value).toLocaleDateString();

const openCreateDialog = () => {
	tokenName.value = '';
	createDialog.value = true;
};

const handleCreate = async () => {
	const result = await createForm.value?.validate();
	if (!result?.valid) return;
	createToken(tokenName.value.trim(), created => {
		createDialog.value = false;
		createdToken.value = created;
		createdDialog.value = true;
	});
};

const copyToken = async () => {
	if (!createdToken.value) return;
	try {
		await navigator.clipboard.writeText(createdToken.value.token);
		toast.success(t('settings.api-tokens.copied'));
	} catch {
		toast.error(t('errors.clipboard-failed'));
	}
};

const closeCreatedDialog = () => {
	createdDialog.value = false;
	createdToken.value = null;
};

const openRevokeDialog = (token: ApiTokenDto) => {
	tokenToRevoke.value = token;
	revokeDialog.value = true;
};

const handleRevoke = () => {
	if (!tokenToRevoke.value) return;
	revokeToken(tokenToRevoke.value.id, () => {
		revokeDialog.value = false;
		tokenToRevoke.value = null;
	});
};

onMounted(() => {
	fetchTokens();
});
</script>
