<template>
	<v-card elevation="0" variant="outlined">
		<v-card-title class="bg-grey-lighten-4 d-flex align-center">
			<v-icon class="mr-2" icon="mdi-robot-outline" />
			{{ t('settings.api-tokens.connect-claude-title') }}
		</v-card-title>
		<v-card-text class="pt-4">
			<p class="text-body-2 text-medium-emphasis mb-4">
				{{ t('settings.api-tokens.connect-claude-description') }}
			</p>

			<h4 class="text-subtitle-2 mb-2">{{ t('settings.api-tokens.connect-web-title') }}</h4>
			<p class="text-body-2 text-medium-emphasis mb-2">
				{{ t('settings.api-tokens.connect-web-description') }}
			</p>
			<div class="d-flex align-center command-box pa-3 mb-2">
				<code class="command-value">{{ mcpUrl }}</code>
				<v-btn
					:aria-label="t('settings.api-tokens.copy')"
					icon="mdi-content-copy"
					size="small"
					variant="text"
					@click="copy(mcpUrl)"
				/>
			</div>
			<p class="text-caption text-medium-emphasis mb-6">
				{{ t('settings.api-tokens.connect-web-oauth-hint') }}
			</p>

			<h4 class="text-subtitle-2 mb-2">{{ t('settings.api-tokens.connect-cli-title') }}</h4>
			<ol class="text-body-2 mb-4 ml-4">
				<li>{{ t('settings.api-tokens.connect-claude-step-token') }}</li>
				<li>{{ t('settings.api-tokens.connect-claude-step-command') }}</li>
			</ol>
			<div class="d-flex align-center command-box pa-3">
				<code class="command-value">{{ mcpAddCommand }}</code>
				<v-btn
					:aria-label="t('settings.api-tokens.copy')"
					icon="mdi-content-copy"
					size="small"
					variant="text"
					@click="copy(mcpAddCommand)"
				/>
			</div>
		</v-card-text>
	</v-card>
</template>

<style scoped>
.command-box {
	background-color: rgba(var(--v-theme-on-surface), 0.05);
	border-radius: 4px;
}

.command-value {
	flex: 1;
	word-break: break-all;
}
</style>

<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { toast } from 'vuetify-sonner';

import { getBackendUrl } from '~/utils/urlUtils';

const { t } = useI18n();

const mcpUrl = `${getBackendUrl()}/mcp`;
const mcpAddCommand = `claude mcp add --transport http kerflow ${mcpUrl} --header "Authorization: Bearer kf_YOUR_TOKEN"`;

const copy = async (value: string) => {
	try {
		await navigator.clipboard.writeText(value);
		toast.success(t('settings.api-tokens.copied'));
	} catch {
		toast.error(t('errors.clipboard-failed'));
	}
};
</script>
