<template>
	<div class="d-flex align-center copyable-box pa-3">
		<code class="copyable-value">{{ value }}</code>
		<v-btn :aria-label="t('common.copy')" icon="mdi-content-copy" size="small" variant="text" @click="copy" />
	</div>
</template>

<style scoped>
.copyable-box {
	background-color: rgba(var(--v-theme-on-surface), 0.05);
	border-radius: 4px;
}

.copyable-value {
	flex: 1;
	word-break: break-all;
}
</style>

<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { toast } from '~/composables/useToast';

interface Props {
	value: string;
}

const props = defineProps<Props>();

const { t } = useI18n();

const copy = async () => {
	try {
		await navigator.clipboard.writeText(props.value);
		toast.success(t('common.copied'));
	} catch {
		toast.error(t('errors.clipboard-failed'));
	}
};
</script>
