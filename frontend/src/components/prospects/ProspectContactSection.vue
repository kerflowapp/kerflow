<template>
	<v-list-subheader>{{ t('prospects.details.sections.contact') }}</v-list-subheader>

	<InlineEditableField
		:empty-text="t('prospects.details.empty')"
		icon="mdi-phone-outline"
		:label="t('prospects.fields.phone')"
		:loading="loading"
		:model-value="prospect.phone"
		:readonly="readonly"
		@save="emit('update', { phone: $event })"
	>
		<template #actions>
			<v-btn :aria-label="t('common.copy')" icon="mdi-content-copy" size="small" variant="text" @click.stop="copyText(prospect.phone)" />
		</template>
	</InlineEditableField>

	<InlineEditableField
		:empty-text="t('prospects.details.empty')"
		icon="mdi-email-outline"
		:label="t('prospects.fields.email')"
		:loading="loading"
		:model-value="prospect.email"
		:readonly="readonly"
		@save="emit('update', { email: $event })"
	>
		<template #actions>
			<v-btn :aria-label="t('common.copy')" icon="mdi-content-copy" size="small" variant="text" @click.stop="copyText(prospect.email)" />
		</template>
	</InlineEditableField>

	<v-divider class="my-2" />

	<v-list-subheader>{{ t('prospects.details.sections.address') }}</v-list-subheader>

	<InlineEditableField
		:empty-text="t('prospects.details.empty')"
		icon="mdi-map-marker-outline"
		:label="t('prospects.fields.address')"
		:loading="loading"
		:model-value="prospect.address"
		:readonly="readonly"
		@save="emit('update', { address: $event })"
	>
		<template #actions>
			<v-btn :aria-label="t('prospects.details.actions.open-maps')" icon="mdi-map" size="small" variant="text" @click.stop="openMaps" />
			<v-btn :aria-label="t('common.copy')" icon="mdi-content-copy" size="small" variant="text" @click.stop="copyText(prospect.address)" />
		</template>
	</InlineEditableField>

	<v-divider class="my-2" />

	<v-list-subheader>{{ t('prospects.details.sections.links') }}</v-list-subheader>

	<InlineEditableField
		v-for="link in links"
		:key="link.key"
		:empty-text="t('prospects.details.empty')"
		:icon="link.icon"
		:label="link.label"
		:loading="loading"
		:model-value="link.value"
		:readonly="readonly"
		@save="saveLink(link.key, $event)"
	>
		<template #actions>
			<v-btn
				:aria-label="t('prospects.details.actions.open-link')"
				icon="mdi-open-in-new"
				size="small"
				variant="text"
				@click.stop="openUrl(link.value)"
			/>
			<v-btn :aria-label="t('common.copy')" icon="mdi-content-copy" size="small" variant="text" @click.stop="copyText(link.value)" />
		</template>
	</InlineEditableField>

	<v-divider class="my-2" />

	<v-list-subheader>{{ t('prospects.details.sections.notes') }}</v-list-subheader>

	<InlineEditableField
		:empty-text="t('prospects.details.no-notes')"
		:label="t('prospects.fields.notes')"
		:loading="loading"
		:model-value="prospect.notes"
		multiline
		:readonly="readonly"
		@save="emit('update', { notes: $event })"
	/>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { toast } from '~/composables/useToast';

import type { ProspectDto, UpdateProspectDto } from '~/api/dtos/prospect.dto';
import InlineEditableField from '~/components/prospects/InlineEditableField.vue';

type LinkKey = 'website' | 'instagram' | 'facebook' | 'linkedin';

interface Props {
	prospect: ProspectDto;
	readonly?: boolean;
	loading?: boolean;
}

interface Emits {
	(e: 'update', data: UpdateProspectDto): void;
}

const props = withDefaults(defineProps<Props>(), {
	readonly: false,
	loading: false
});
const emit = defineEmits<Emits>();
const { t } = useI18n();

const links = computed<{ key: LinkKey; icon: string; label: string; value?: string | null }[]>(() => [
	{ key: 'website', icon: 'mdi-web', label: t('prospects.fields.website'), value: props.prospect.website },
	{ key: 'instagram', icon: 'mdi-instagram', label: t('prospects.fields.instagram'), value: props.prospect.socialLinks?.instagram },
	{ key: 'facebook', icon: 'mdi-facebook', label: t('prospects.fields.facebook'), value: props.prospect.socialLinks?.facebook },
	{ key: 'linkedin', icon: 'mdi-linkedin', label: t('prospects.fields.linkedin'), value: props.prospect.socialLinks?.linkedin }
]);

// The backend ignores absent fields: send only the edited one, an empty string clears it
const saveLink = (key: LinkKey, value: string) => {
	emit('update', key === 'website' ? { website: value } : { socialLinks: { [key]: value } });
};

const normalizeUrl = (raw: string) => {
	const trimmed = raw.trim();
	if (!trimmed) return null;
	if (trimmed.startsWith('http://') || trimmed.startsWith('https://')) return trimmed;
	return `https://${trimmed}`;
};

const openUrl = (raw?: string | null) => {
	const url = raw ? normalizeUrl(raw) : null;
	if (!url) return;
	window.open(url, '_blank', 'noopener,noreferrer');
};

const openMaps = () => {
	const address = props.prospect.address?.trim();
	if (!address) return;
	const url = `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(address)}`;
	window.open(url, '_blank', 'noopener,noreferrer');
};

const copyText = async (value?: string | null) => {
	if (!value) return;
	try {
		await navigator.clipboard.writeText(value);
		toast.success(t('prospects.details.copy-success'));
	} catch {
		toast.error(t('errors.clipboard-failed'));
	}
};
</script>
