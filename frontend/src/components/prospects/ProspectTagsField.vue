<template>
	<v-combobox
		v-if="isEditing"
		v-model="draft"
		autofocus
		chips
		closable-chips
		density="compact"
		hide-details
		:label="t('prospects.fields.tags')"
		:loading="loading"
		multiple
		variant="outlined"
		@keydown.esc.stop.prevent="cancel"
		@update:focused="onFocusChange"
	/>

	<div v-else class="d-flex flex-wrap align-center ga-2">
		<v-chip v-for="tag in modelValue" :key="tag" color="primary" size="small" variant="tonal">
			{{ tag }}
		</v-chip>
		<v-chip v-if="!readonly" prepend-icon="mdi-tag-plus-outline" size="small" variant="outlined" @click="startEditing">
			{{ modelValue.length ? t('common.edit') : t('prospects.details.add-tag') }}
		</v-chip>
	</div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';

import { normalizeTags, sameTags } from '~/utils/inline-edit';

interface Props {
	modelValue?: string[];
	readonly?: boolean;
	loading?: boolean;
}

interface Emits {
	(e: 'save', tags: string[]): void;
}

const props = withDefaults(defineProps<Props>(), {
	modelValue: () => [],
	readonly: false,
	loading: false
});
const emit = defineEmits<Emits>();
const { t } = useI18n();

const isEditing = ref(false);
const draft = ref<string[]>([]);

const startEditing = () => {
	if (props.readonly) return;
	draft.value = [...props.modelValue];
	isEditing.value = true;
};

const cancel = () => {
	isEditing.value = false;
};

const onFocusChange = (focused: boolean) => {
	if (!focused) commit();
};

const commit = () => {
	if (!isEditing.value) return;
	if (sameTags(draft.value, props.modelValue)) {
		cancel();
		return;
	}
	emit('save', normalizeTags(draft.value));
};

// The saved prospect comes back through the prop: reaching the draft closes the field
watch(
	() => props.modelValue,
	tags => {
		if (isEditing.value && sameTags(tags, draft.value)) cancel();
	}
);
</script>
