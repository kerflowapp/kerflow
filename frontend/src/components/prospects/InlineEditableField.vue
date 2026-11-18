<template>
	<div v-if="isEditing" class="px-4 py-1">
		<v-textarea
			v-if="multiline"
			v-model="draft"
			auto-grow
			autofocus
			density="compact"
			hide-details="auto"
			:label="label"
			:loading="loading"
			:prepend-inner-icon="icon"
			rows="2"
			variant="outlined"
			@keydown.ctrl.enter.prevent="commit"
			@keydown.esc.stop.prevent="cancel"
			@keydown.meta.enter.prevent="commit"
			@update:focused="onFocusChange"
		>
			<template #append-inner>
				<InlineEditActions @cancel="cancel" @commit="commit" />
			</template>
		</v-textarea>
		<v-text-field
			v-else
			v-model="draft"
			autofocus
			density="compact"
			hide-details="auto"
			:label="label"
			:loading="loading"
			:prepend-inner-icon="icon"
			:rules="rules"
			variant="outlined"
			@keydown.enter.prevent="commit"
			@keydown.esc.stop.prevent="cancel"
			@update:focused="onFocusChange"
		>
			<template #append-inner>
				<InlineEditActions @cancel="cancel" @commit="commit" />
			</template>
		</v-text-field>
	</div>

	<v-list-item
		v-else
		:aria-label="readonly ? undefined : t('prospects.details.click-to-edit')"
		:class="{ 'text-medium-emphasis': !modelValue }"
		:link="!readonly"
		:prepend-icon="icon"
		@click="startEditing"
	>
		<v-list-item-title :class="multiline ? 'multiline-value' : undefined">
			{{ modelValue || emptyText }}
		</v-list-item-title>
		<template v-if="modelValue && $slots.actions" #append>
			<slot name="actions" />
		</template>
	</v-list-item>
</template>

<style scoped>
.multiline-value {
	white-space: pre-wrap;
	line-height: 1.5;
}
</style>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';

import InlineEditActions from '~/components/prospects/InlineEditActions.vue';
import { resolveInlineCommit } from '~/utils/inline-edit';

interface Props {
	modelValue?: string | null;
	icon?: string;
	label: string;
	emptyText: string;
	multiline?: boolean;
	readonly?: boolean;
	loading?: boolean;
	required?: boolean;
}

interface Emits {
	(e: 'save', value: string): void;
}

const props = withDefaults(defineProps<Props>(), {
	modelValue: null,
	icon: undefined,
	multiline: false,
	readonly: false,
	loading: false,
	required: false
});
const emit = defineEmits<Emits>();
const { t } = useI18n();

const isEditing = ref(false);
const draft = ref('');
// The value already sent: Enter then blur must not save it twice while the request is in flight
const pending = ref<string | null>(null);

const rules = computed(() => (props.required ? [(v: string) => !!v?.trim() || t('validation.required')] : []));

const startEditing = () => {
	if (props.readonly) return;
	draft.value = props.modelValue ?? '';
	pending.value = null;
	isEditing.value = true;
};

const cancel = () => {
	isEditing.value = false;
	pending.value = null;
};

const onFocusChange = (focused: boolean) => {
	if (!focused) commit();
};

const commit = () => {
	if (!isEditing.value) return;
	const result = resolveInlineCommit(draft.value, props.modelValue, props.required);
	if (result.kind === 'unchanged') {
		cancel();
		return;
	}
	if (result.kind === 'invalid' || result.value === pending.value) return;
	pending.value = result.value;
	emit('save', result.value);
};

// The saved prospect comes back through the prop: reaching the draft is the success signal that closes the field
watch(
	() => props.modelValue,
	value => {
		if (isEditing.value && (value ?? '') === draft.value.trim()) cancel();
	}
);

// A failed save leaves the field open: let the same value be sent again
watch(
	() => props.loading,
	isLoading => {
		if (!isLoading && isEditing.value) pending.value = null;
	}
);
</script>
