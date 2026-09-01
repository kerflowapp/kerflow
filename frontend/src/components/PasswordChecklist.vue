<template>
	<v-expand-transition>
		<div v-if="showChecklist" class="mb-2">
			<div class="text-caption text-medium-emphasis mb-1">{{ t('auth.password-checklist.title') }}</div>
			<v-list class="pa-0 bg-background" density="compact">
				<v-list-item v-for="rule in rules" :key="rule.key" class="pa-0" min-height="28">
					<template #prepend>
						<v-icon
							class="mr-2"
							:color="rule.valid ? 'success' : 'grey-lighten-1'"
							:icon="rule.valid ? 'mdi-check-circle' : 'mdi-circle-outline'"
							size="18"
						/>
					</template>
					<v-list-item-title class="text-caption" :class="rule.valid ? 'text-success' : 'text-medium-emphasis'">
						{{ t(`auth.password-checklist.${rule.key}`) }}
					</v-list-item-title>
				</v-list-item>
			</v-list>
		</div>
	</v-expand-transition>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue';
import { useI18n } from 'vue-i18n';

interface Props {
	password: string;
}

const props = defineProps<Props>();

const model = defineModel<boolean>({ default: false });

const { t } = useI18n();

const rules = computed(() => [
	{ key: 'min-length', valid: props.password.length >= 8 },
	{ key: 'uppercase', valid: /[A-Z]/.test(props.password) },
	{ key: 'lowercase', valid: /[a-z]/.test(props.password) },
	{ key: 'number', valid: /\d/.test(props.password) },
	{ key: 'special', valid: /[^A-Za-z\d]/.test(props.password) },
	{ key: 'no-spaces', valid: !props.password.includes(' ') }
]);

const isValid = computed(() => rules.value.every(r => r.valid));
const showChecklist = computed(() => props.password.length > 0 && !isValid.value);

watch(
	isValid,
	val => {
		model.value = val;
	},
	{ immediate: true }
);
</script>
