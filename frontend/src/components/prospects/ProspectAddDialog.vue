<template>
	<v-dialog max-width="500" :model-value="modelValue" @update:model-value="emit('update:modelValue', $event)">
		<v-card elevation="0">
			<v-card-title>{{ t('prospects.add-manually') }}</v-card-title>
			<v-card-text>
				<v-form ref="formRef" @submit.prevent="handleSubmit">
					<v-text-field
						v-model="form.name"
						class="mb-2"
						density="comfortable"
						:label="t('prospects.fields.name')"
						:rules="requiredRule"
						variant="outlined"
					/>
					<v-select
						v-model="form.statusKey"
						class="mb-2"
						density="comfortable"
						:item-title="columnTitle"
						item-value="key"
						:items="columns"
						:label="t('prospects.fields.column')"
						variant="outlined"
					/>
					<div class="text-caption text-medium-emphasis mb-1">{{ t('prospects.fields.placement') }}</div>
					<v-btn-toggle
						v-model="form.placement"
						class="mb-4"
						color="primary"
						density="compact"
						divided
						mandatory
						variant="outlined"
					>
						<v-btn prepend-icon="mdi-arrow-collapse-up" :value="ProspectPlacement.TOP">
							{{ t('prospects.placement.top') }}
						</v-btn>
						<v-btn prepend-icon="mdi-arrow-collapse-down" :value="ProspectPlacement.BOTTOM">
							{{ t('prospects.placement.bottom') }}
						</v-btn>
					</v-btn-toggle>
					<v-text-field
						v-model="form.phone"
						class="mb-2"
						density="comfortable"
						:label="t('prospects.fields.phone')"
						variant="outlined"
					/>
					<v-text-field
						v-model="form.email"
						class="mb-2"
						density="comfortable"
						:label="t('prospects.fields.email')"
						variant="outlined"
					/>
					<v-text-field
						v-model="form.website"
						density="comfortable"
						:label="t('prospects.fields.website')"
						variant="outlined"
					/>
				</v-form>
			</v-card-text>
			<v-card-actions>
				<v-spacer />
				<v-btn variant="text" @click="emit('update:modelValue', false)">{{ t('common.cancel') }}</v-btn>
				<v-btn color="primary" :loading="isCreating" variant="flat" @click="handleSubmit">
					{{ t('common.add') }}
				</v-btn>
			</v-card-actions>
		</v-card>
	</v-dialog>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import type { VForm } from 'vuetify/components';

import type { CreateProspectDto, ProspectPipelineColumnDto } from '~/api/dtos/prospect.dto';
import { ProspectPlacement, ProspectSource } from '~/api/dtos/prospect.dto';
import { useProspectPipeline } from '~/composables/useProspectPipeline';

interface Props {
	modelValue: boolean;
	columns: ProspectPipelineColumnDto[];
	initialStatusKey: string;
	isCreating?: boolean;
}

interface Emits {
	(e: 'update:modelValue', value: boolean): void;
	(e: 'submit', data: CreateProspectDto): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
const { t } = useI18n();
const { columnTitle } = useProspectPipeline();

const formRef = ref<VForm | null>(null);
const form = reactive({
	name: '',
	phone: '',
	email: '',
	website: '',
	statusKey: props.initialStatusKey,
	placement: ProspectPlacement.TOP
});

const requiredRule = [(v: string) => !!v || t('validation.required')];

// Each opening starts from a blank form placed in the column it was opened from
watch(
	() => props.modelValue,
	open => {
		if (!open) return;
		form.name = '';
		form.phone = '';
		form.email = '';
		form.website = '';
		form.statusKey = props.initialStatusKey;
		form.placement = ProspectPlacement.TOP;
		formRef.value?.resetValidation();
	}
);

const handleSubmit = async () => {
	const result = await formRef.value?.validate();
	if (result && !result.valid) return;
	if (!form.name) return;
	emit('submit', {
		name: form.name,
		phone: form.phone,
		email: form.email,
		website: form.website,
		source: ProspectSource.MANUAL,
		statusKey: form.statusKey,
		placement: form.placement
	});
};
</script>
