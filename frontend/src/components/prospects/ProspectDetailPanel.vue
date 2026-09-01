<template>
	<v-navigation-drawer v-model="modelValue" location="right" temporary width="450">
		<v-card v-if="prospect" flat>
			<v-card-title class="d-flex justify-space-between align-center pa-4">
				<span>{{ isEditing ? t('prospects.edit-prospect') : t('prospects.prospect-details') }}</span>
				<v-btn icon="mdi-close" variant="text" @click="modelValue = false" />
			</v-card-title>

			<v-card-text>
				<v-form ref="formRef" @submit.prevent="saveChanges">
					<v-text-field
						v-model="form.name"
						class="mb-2"
						density="comfortable"
						:label="t('prospects.fields.name')"
						:readonly="!isEditing"
						:rules="requiredRule"
						variant="outlined"
					/>
					<v-text-field
						v-model="form.address"
						class="mb-2"
						density="comfortable"
						:label="t('prospects.fields.address')"
						prepend-inner-icon="mdi-map-marker-outline"
						:readonly="!isEditing"
						variant="outlined"
					/>
					<v-text-field
						v-model="form.phone"
						class="mb-2"
						density="comfortable"
						:label="t('prospects.fields.phone')"
						prepend-inner-icon="mdi-phone-outline"
						:readonly="!isEditing"
						variant="outlined"
					/>
					<v-text-field
						v-model="form.email"
						class="mb-2"
						density="comfortable"
						:label="t('prospects.fields.email')"
						prepend-inner-icon="mdi-email-outline"
						:readonly="!isEditing"
						variant="outlined"
					/>
					<v-text-field
						v-model="form.website"
						class="mb-2"
						density="comfortable"
						:label="t('prospects.fields.website')"
						prepend-inner-icon="mdi-web"
						:readonly="!isEditing"
						variant="outlined"
					/>

					<v-divider class="my-3" />

					<div class="text-subtitle-2 font-weight-bold mb-2">{{ t('prospects.fields.social-links') }}</div>

					<v-text-field
						v-model="form.instagram"
						class="mb-2"
						density="comfortable"
						:label="t('prospects.fields.instagram')"
						prepend-inner-icon="mdi-instagram"
						:readonly="!isEditing"
						variant="outlined"
					/>
					<v-text-field
						v-model="form.facebook"
						class="mb-2"
						density="comfortable"
						:label="t('prospects.fields.facebook')"
						prepend-inner-icon="mdi-facebook"
						:readonly="!isEditing"
						variant="outlined"
					/>
					<v-text-field
						v-model="form.linkedin"
						class="mb-2"
						density="comfortable"
						:label="t('prospects.fields.linkedin')"
						prepend-inner-icon="mdi-linkedin"
						:readonly="!isEditing"
						variant="outlined"
					/>

					<v-divider class="my-3" />

					<v-textarea
						v-model="form.notes"
						density="comfortable"
						:label="t('prospects.fields.notes')"
						:readonly="!isEditing"
						rows="3"
						variant="outlined"
					/>

					<v-combobox
						v-model="form.tags"
						chips
						class="mt-2"
						closable-chips
						density="comfortable"
						:label="t('prospects.fields.tags')"
						multiple
						:readonly="!isEditing"
						variant="outlined"
					/>
				</v-form>
			</v-card-text>

			<v-card-actions class="pa-4">
				<v-btn v-if="!isEditing" color="primary" variant="outlined" @click="isEditing = true">
					<v-icon start>mdi-pencil</v-icon>
					{{ t('common.edit') }}
				</v-btn>
				<template v-else>
					<v-btn variant="text" @click="cancelEdit">{{ t('common.cancel') }}</v-btn>
					<v-spacer />
					<v-btn color="primary" :loading="isUpdating" variant="flat" @click="saveChanges">
						{{ t('common.save') }}
					</v-btn>
				</template>
			</v-card-actions>
		</v-card>
	</v-navigation-drawer>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';

import type { ProspectDto, UpdateProspectDto } from '~/api/dtos/prospect.dto';

interface Props {
	prospect: ProspectDto | null;
	isUpdating?: boolean;
}

interface Emits {
	(e: 'update', payload: { id: string; data: UpdateProspectDto }): void;
}

const props = withDefaults(defineProps<Props>(), { isUpdating: false });
const emit = defineEmits<Emits>();
const { t } = useI18n();

const modelValue = defineModel<boolean>();
const isEditing = ref(false);

const form = reactive({
	name: '',
	address: '',
	phone: '',
	email: '',
	website: '',
	instagram: '',
	facebook: '',
	linkedin: '',
	notes: '',
	tags: [] as string[]
});

const requiredRule = [(v: string) => !!v || t('validation.required')];

watch(
	() => props.prospect,
	p => {
		if (p) {
			form.name = p.name || '';
			form.address = p.address || '';
			form.phone = p.phone || '';
			form.email = p.email || '';
			form.website = p.website || '';
			form.instagram = p.socialLinks?.instagram || '';
			form.facebook = p.socialLinks?.facebook || '';
			form.linkedin = p.socialLinks?.linkedin || '';
			form.notes = p.notes || '';
			form.tags = [...(p.tags || [])];
			isEditing.value = false;
		}
	},
	{ immediate: true }
);

const cancelEdit = () => {
	isEditing.value = false;
	if (props.prospect) {
		form.name = props.prospect.name || '';
		form.address = props.prospect.address || '';
		form.phone = props.prospect.phone || '';
		form.email = props.prospect.email || '';
		form.website = props.prospect.website || '';
		form.instagram = props.prospect.socialLinks?.instagram || '';
		form.facebook = props.prospect.socialLinks?.facebook || '';
		form.linkedin = props.prospect.socialLinks?.linkedin || '';
		form.notes = props.prospect.notes || '';
		form.tags = [...(props.prospect.tags || [])];
	}
};

const saveChanges = () => {
	if (!props.prospect) return;
	const data: UpdateProspectDto = {
		name: form.name,
		address: form.address,
		phone: form.phone,
		email: form.email,
		website: form.website,
		socialLinks: {
			instagram: form.instagram || undefined,
			facebook: form.facebook || undefined,
			linkedin: form.linkedin || undefined
		},
		notes: form.notes,
		tags: form.tags
	};
	emit('update', { id: props.prospect.id, data });
	isEditing.value = false;
};
</script>
