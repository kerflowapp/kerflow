<template>
	<v-card elevation="0" variant="outlined">
		<v-card-title class="bg-grey-lighten-4 d-flex align-center">
			<v-icon class="mr-2" icon="mdi-robot-outline" />
			{{ t('settings.mcp-guide.title') }}
		</v-card-title>
		<v-card-text class="pt-4">
			<p class="text-body-2 text-medium-emphasis mb-4">{{ t('settings.mcp-guide.intro') }}</p>

			<h4 class="text-subtitle-2 mb-2">{{ t('settings.mcp-guide.server-url') }}</h4>
			<copyable-value class="mb-4" :value="mcpUrl" />

			<v-row class="mb-2" dense>
				<v-col v-for="mode in modes" :key="mode.key" cols="12" md="6">
					<v-sheet border class="pa-3 h-100" rounded>
						<div class="d-flex align-center text-subtitle-2 mb-1">
							<v-icon class="mr-2" :icon="mode.icon" size="small" />
							{{ t(`settings.mcp-guide.${mode.key}-title`) }}
						</div>
						<p class="text-body-2 text-medium-emphasis">{{ t(`settings.mcp-guide.${mode.key}-text`) }}</p>
					</v-sheet>
				</v-col>
			</v-row>

			<v-tabs v-model="activeTab" class="mb-4" color="primary" show-arrows>
				<v-tab v-for="guide in guides" :key="guide.key" :value="guide.key">
					{{ t(`settings.mcp-guide.tabs.${guide.key}`) }}
				</v-tab>
			</v-tabs>

			<v-tabs-window v-model="activeTab">
				<v-tabs-window-item v-for="guide in guides" :key="guide.key" :value="guide.key">
					<h4 class="text-subtitle-2 mb-2">{{ t('settings.mcp-guide.connect-title') }}</h4>
					<ol class="text-body-2 mb-3 ml-4">
						<li v-for="step in guide.connect" :key="step.key" class="mb-2">
							{{ t(step.key) }}
							<copyable-value v-if="step.command" class="mt-2" :value="step.command" />
						</li>
					</ol>
					<v-alert class="mb-4" density="compact" type="info" variant="tonal">
						{{ t(`settings.mcp-guide.${guide.key}.note`) }}
					</v-alert>

					<h4 class="text-subtitle-2 mb-2">{{ t('settings.mcp-guide.revoke-title') }}</h4>
					<ol class="text-body-2 ml-4">
						<li v-for="step in guide.revoke" :key="step.key" class="mb-2">
							{{ t(step.key) }}
							<copyable-value v-if="step.command" class="mt-2" :value="step.command" />
						</li>
					</ol>
				</v-tabs-window-item>
			</v-tabs-window>
		</v-card-text>
	</v-card>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';

import CopyableValue from '~/components/CopyableValue.vue';
import { getBackendUrl } from '~/utils/urlUtils';

interface GuideStep {
	key: string;
	command?: string;
}

interface Guide {
	key: 'claude' | 'chatgpt' | 'claude-code' | 'other';
	connect: GuideStep[];
	revoke: GuideStep[];
}

const { t } = useI18n();

const mcpUrl = `${getBackendUrl()}/mcp`;

const modes = [
	{ key: 'oauth-mode', icon: 'mdi-shield-check-outline' },
	{ key: 'token-mode', icon: 'mdi-key-outline' }
];

const steps = (prefix: string, count: number): GuideStep[] =>
	Array.from({ length: count }, (_, index) => ({ key: `settings.mcp-guide.${prefix}-step-${index + 1}` }));

const revokeInKerflow: GuideStep = { key: 'settings.mcp-guide.revoke-in-kerflow' };

const guides: Guide[] = [
	{
		key: 'claude',
		connect: steps('claude.connect', 4),
		revoke: [revokeInKerflow, { key: 'settings.mcp-guide.claude.revoke-step-2' }]
	},
	{
		key: 'chatgpt',
		connect: steps('chatgpt.connect', 5),
		revoke: [revokeInKerflow, { key: 'settings.mcp-guide.chatgpt.revoke-step-2' }]
	},
	{
		key: 'claude-code',
		connect: [
			{ key: 'settings.mcp-guide.claude-code.connect-step-1' },
			{
				key: 'settings.mcp-guide.claude-code.connect-step-2',
				command: `claude mcp add --transport http kerflow ${mcpUrl} --header "Authorization: Bearer kf_YOUR_TOKEN"`
			}
		],
		revoke: [
			{ key: 'settings.mcp-guide.claude-code.revoke-step-1' },
			{ key: 'settings.mcp-guide.claude-code.revoke-step-2', command: 'claude mcp remove kerflow' }
		]
	},
	{
		key: 'other',
		connect: [
			{ key: 'settings.mcp-guide.other.connect-step-1' },
			{ key: 'settings.mcp-guide.other.connect-step-2' },
			{ key: 'settings.mcp-guide.other.connect-step-3', command: 'Authorization: Bearer kf_YOUR_TOKEN' }
		],
		revoke: steps('other.revoke', 2)
	}
];

const activeTab = ref<Guide['key']>('claude');
</script>
