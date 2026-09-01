<template>
	<v-navigation-drawer v-model="modelValue" location="right" temporary :width="computedWidth">
		<v-card v-if="prospect" class="d-flex flex-column" elevation="0" height="100%">
			<v-card-title class="d-flex justify-space-between align-center pa-4">
				<span>{{ t('prospects.edit-prospect') }}</span>
				<v-btn icon="mdi-close" variant="text" @click="modelValue = false" />
			</v-card-title>

			<v-card-text class="flex-1-1-0 overflow-y-auto">
				<v-form ref="formRef" @submit.prevent="saveChanges">
					<v-text-field
						v-model="form.name"
						class="mb-2"
						density="comfortable"
						:label="t('prospects.fields.name')"
						:rules="requiredRule"
						variant="outlined"
					/>
					<v-text-field
						v-model="form.address"
						class="mb-2"
						density="comfortable"
						:label="t('prospects.fields.address')"
						prepend-inner-icon="mdi-map-marker-outline"
						variant="outlined"
					/>
					<v-text-field
						v-model="form.phone"
						class="mb-2"
						density="comfortable"
						:label="t('prospects.fields.phone')"
						prepend-inner-icon="mdi-phone-outline"
						variant="outlined"
					/>
					<v-text-field
						v-model="form.email"
						class="mb-2"
						density="comfortable"
						:label="t('prospects.fields.email')"
						prepend-inner-icon="mdi-email-outline"
						variant="outlined"
					/>
					<v-text-field
						v-model="form.website"
						class="mb-2"
						density="comfortable"
						:label="t('prospects.fields.website')"
						prepend-inner-icon="mdi-web"
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
						variant="outlined"
					/>
					<v-text-field
						v-model="form.facebook"
						class="mb-2"
						density="comfortable"
						:label="t('prospects.fields.facebook')"
						prepend-inner-icon="mdi-facebook"
						variant="outlined"
					/>
					<v-text-field
						v-model="form.linkedin"
						class="mb-2"
						density="comfortable"
						:label="t('prospects.fields.linkedin')"
						prepend-inner-icon="mdi-linkedin"
						variant="outlined"
					/>

					<v-divider class="my-3" />

					<v-textarea
						v-model="form.notes"
						density="comfortable"
						:label="t('prospects.fields.notes')"
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
						variant="outlined"
					/>
				</v-form>
			</v-card-text>

			<v-card-actions class="pa-4 actions-fixed">
				<v-btn variant="text" @click="modelValue = false">{{ t('common.cancel') }}</v-btn>
				<v-spacer />
				<v-btn color="primary" :loading="isUpdating" variant="flat" @click="saveChanges">
					{{ t('common.save') }}
				</v-btn>
			</v-card-actions>
		</v-card>
	</v-navigation-drawer>
</template>

<style scoped>
.actions-fixed {
	flex: 0 0 auto;
	border-top: 1px solid rgba(var(--v-border-color), var(--v-border-opacity));
}
</style>

<script setup lang="ts">
import { computed, reactive, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useDisplay } from 'vuetify';

import type { ProspectDto, UpdateProspectDto } from '~/api/dtos/prospect.dto';

interface Props {
	prospect: ProspectDto | null;
	isUpdating?: boolean;
}

interface Emits {
	(e: 'update', payload: { id: string; data: UpdateProspectDto }): void;
}

const props = withDefaults(defineProps<Props>(), {
	isUpdating: false
});
const emit = defineEmits<Emits>();
const { t } = useI18n();
const { mobile, width } = useDisplay();

const computedWidth = computed(() => {
	if (mobile.value) {
		return width.value;
	}
	return width.value * 0.35;
});

const modelValue = defineModel<boolean>();

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
		if (!p) return;
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
	},
	{ immediate: true }
);

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
	modelValue.value = false;
};
</script>
