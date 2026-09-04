<template>
	<v-app>
		<router-view></router-view>
		<VSonner :duration="3000" position="bottom-right" :visible-toasts="5"></VSonner>
	</v-app>
</template>

<script setup lang="ts">
import { onMounted, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { VSonner, toast } from 'vuetify-sonner';

import { useBetaMode } from '~/composables/useBetaMode';
import { useSandboxMode } from '~/composables/useSandboxMode';

import 'vuetify-sonner/style.css';

const { isBetaEnabled, initBetaMode } = useBetaMode();
const { isSandboxEnabled, initSandboxMode } = useSandboxMode();
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
