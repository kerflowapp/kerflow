<template>
	<v-app>
		<router-view></router-view>
		<v-snackbar-queue v-model="messages" location="bottom right" :timeout="3000" :total-visible="5"></v-snackbar-queue>
	</v-app>
</template>

<script setup lang="ts">
import { onMounted, watch } from 'vue';
import { useI18n } from 'vue-i18n';

import { useBetaMode } from '~/composables/useBetaMode';
import { useSandboxMode } from '~/composables/useSandboxMode';
import { useToast } from '~/composables/useToast';

const { isBetaEnabled, initBetaMode } = useBetaMode();
const { isSandboxEnabled, initSandboxMode } = useSandboxMode();
const { messages, toast } = useToast();
const { t } = useI18n();

watch(isBetaEnabled, newValue => {
	if (newValue) {
		toast.success(t('beta.enabled'));
	}
});

watch(isSandboxEnabled, newValue => {
	if (newValue) {
		toast.success(t('common.sandbox-enabled'));
	}
});

onMounted(() => {
	initBetaMode();
	initSandboxMode();
});
</script>
