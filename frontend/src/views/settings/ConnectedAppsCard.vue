<template>
	<v-card elevation="0" variant="outlined">
		<v-card-title class="bg-grey-lighten-4 d-flex align-center">
			<v-icon class="mr-2" icon="mdi-apps" />
			{{ t('settings.connected-apps.title') }}
		</v-card-title>
		<v-card-text class="pt-4">
			<p class="text-body-2 text-medium-emphasis mb-4">{{ t('settings.connected-apps.description') }}</p>

			<div v-if="isFetching" class="d-flex justify-center py-6">
				<v-progress-circular indeterminate />
			</div>

			<v-list v-else-if="connections.length > 0" lines="two">
				<v-list-item v-for="connection in connections" :key="connection.clientId">
					<template #prepend>
						<v-icon icon="mdi-connection" />
					</template>
					<v-list-item-title>{{ connection.clientName }}</v-list-item-title>
					<v-list-item-subtitle>
						{{ t('settings.connected-apps.last-activity', { date: formatDate(connection.lastActivity) }) }}
					</v-list-item-subtitle>
					<template #append>
						<v-btn color="error" size="small" variant="text" @click="openRevokeDialog(connection)">
							{{ t('settings.connected-apps.revoke-button') }}
						</v-btn>
					</template>
				</v-list-item>
			</v-list>

			<v-empty-state
				v-else
				icon="mdi-connection"
				:text="t('settings.connected-apps.empty-text')"
				:title="t('settings.connected-apps.empty-title')"
			/>
		</v-card-text>

		<confirmation-dialog v-model="revokeDialog" level="error" :title="t('settings.connected-apps.revoke-confirm-title')">
			<template #message>
				{{ t('settings.connected-apps.revoke-confirm-message', { name: connectionToRevoke?.clientName }) }}
			</template>
			<template #actions>
				<v-btn variant="text" @click="revokeDialog = false">{{ t('common.cancel') }}</v-btn>
				<v-btn color="error" :loading="isRevoking" variant="flat" @click="handleRevoke">
					{{ t('settings.connected-apps.revoke-button') }}
				</v-btn>
			</template>
		</confirmation-dialog>
	</v-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';

import type { OAuthConnectionDto } from '~/api/dtos/oauth-connection.dto';
import ConfirmationDialog from '~/components/ConfirmationDialog.vue';
import { useOAuthConnections } from '~/composables/useOAuthConnections';

const { t } = useI18n();
const { connections, isFetching, isRevoking, fetchConnections, revokeConnection } = useOAuthConnections();

const revokeDialog = ref(false);
const connectionToRevoke = ref<OAuthConnectionDto | null>(null);

const formatDate = (value: string): string => new Date(value).toLocaleDateString();

const openRevokeDialog = (connection: OAuthConnectionDto) => {
	connectionToRevoke.value = connection;
	revokeDialog.value = true;
};

const handleRevoke = () => {
	if (!connectionToRevoke.value) return;
	revokeConnection(connectionToRevoke.value.clientId, () => {
		revokeDialog.value = false;
		connectionToRevoke.value = null;
	});
};

onMounted(() => {
	fetchConnections();
});
</script>
