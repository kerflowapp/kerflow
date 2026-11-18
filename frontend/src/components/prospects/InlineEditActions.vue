<template>
	<!-- mousedown.prevent keeps the focus in the field: its blur would otherwise commit before the click lands -->
	<div class="d-flex align-center">
		<v-btn
			:aria-label="t('common.save')"
			color="primary"
			icon="mdi-check"
			size="x-small"
			variant="text"
			@click="emit('commit')"
			@mousedown.prevent
		/>
		<v-btn
			:aria-label="t('common.cancel')"
			icon="mdi-close"
			size="x-small"
			variant="text"
			@click="emit('cancel')"
			@mousedown.prevent
		/>
	</div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n';

interface Emits {
	(e: 'commit'): void;
	(e: 'cancel'): void;
}

const emit = defineEmits<Emits>();
const { t } = useI18n();
</script>
