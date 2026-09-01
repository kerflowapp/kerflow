<template>
	<v-dialog max-width="480" :model-value="modelValue" @update:model-value="emit('update:modelValue', $event)">
		<v-card elevation="0">
			<v-card-title class="d-flex align-center ga-2">
				<v-icon color="primary" icon="mdi-lock-clock" />
				{{ t('billing.trial-expired.title') }}
			</v-card-title>
			<v-card-text>
				{{ t('billing.trial-expired.message') }}
			</v-card-text>
			<v-card-actions>
				<v-spacer />
				<v-btn color="primary" variant="flat" @click="goToBilling">
					{{ t('billing.trial-expired.cta') }}
				</v-btn>
			</v-card-actions>
		</v-card>
	</v-dialog>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';

const { t } = useI18n();
const router = useRouter();

interface Props {
	modelValue: boolean;
}

defineProps<Props>();

interface Emits {
	(e: 'update:modelValue', value: boolean): void;
}

const emit = defineEmits<Emits>();

const goToBilling = () => {
	emit('update:modelValue', false);
	router.push('/settings/billing');
};
</script>
