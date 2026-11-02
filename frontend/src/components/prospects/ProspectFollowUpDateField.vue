<template>
	<v-menu v-model="isOpen" :close-on-content-click="false" :disabled="disabled">
		<template #activator="{ props: menuProps }">
			<v-text-field
				v-bind="menuProps"
				clearable
				density="comfortable"
				:disabled="disabled"
				hide-details
				:label="t('follow-ups.manual-date')"
				:loading="loading"
				:model-value="displayedDate"
				persistent-clear
				prepend-inner-icon="mdi-bell-outline"
				readonly
				variant="outlined"
				@click:clear="emit('update', null)"
			/>
		</template>
		<!-- The app's Vuetify locale is not wired to vue-i18n: the provider keeps the calendar in the user's language -->
		<v-locale-provider :locale="locale" :messages="VUETIFY_MESSAGES">
			<v-date-picker hide-header :model-value="selectedDate" @update:model-value="selectDate" />
		</v-locale-provider>
	</v-menu>
</template>

<script setup lang="ts">
import { format, parseISO } from 'date-fns';
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { en, fr } from 'vuetify/locale';

interface Props {
	modelValue?: string | null;
	disabled?: boolean;
	loading?: boolean;
}

interface Emits {
	(e: 'update', followUpDate: string | null): void;
}

const VUETIFY_MESSAGES = { en, fr };

const props = withDefaults(defineProps<Props>(), {
	modelValue: null,
	disabled: false,
	loading: false
});
const emit = defineEmits<Emits>();
const { t, locale } = useI18n();

const isOpen = ref(false);

const selectedDate = computed(() => (props.modelValue ? parseISO(props.modelValue) : null));

const displayedDate = computed(
	() =>
		selectedDate.value?.toLocaleDateString(locale.value, { day: 'numeric', month: 'long', year: 'numeric' }) ?? null
);

const selectDate = (date: unknown) => {
	isOpen.value = false;
	if (!(date instanceof Date)) return;
	const followUpDate = format(date, 'yyyy-MM-dd');
	if (followUpDate !== props.modelValue) emit('update', followUpDate);
};
</script>
