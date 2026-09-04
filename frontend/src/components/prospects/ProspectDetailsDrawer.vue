<template>
	<v-navigation-drawer v-model="modelValue" location="right" temporary :width="computedWidth">
		<v-card v-if="prospect" class="h-100 d-flex flex-column" elevation="0">
			<v-card-title class="d-flex align-start pa-4">
				<div class="flex-grow-1 pr-2" style="min-width: 0">
					<div class="text-h6 font-weight-bold text-truncate">{{ prospect.name }}</div>
					<div v-if="prospect.email" class="text-caption text-medium-emphasis text-truncate">
						{{ prospect.email }}
					</div>
				</div>
				<div class="d-flex align-center ga-1 flex-shrink-0">
					<v-btn v-if="!readonly" icon="mdi-pencil-outline" size="small" variant="text" @click="emit('edit')" />
					<v-btn
						v-if="!readonly"
						color="error"
						icon="mdi-delete-outline"
						size="small"
						variant="text"
						@click="confirmDelete = true"
					/>
					<v-btn icon="mdi-close" size="small" variant="text" @click="modelValue = false" />
				</div>
			</v-card-title>

			<div v-if="prospect.tags?.length" class="px-4 pb-3">
				<div class="d-flex flex-wrap ga-2">
					<v-chip v-for="tag in prospect.tags" :key="tag" color="primary" size="small" variant="tonal">
						{{ tag }}
					</v-chip>
				</div>
			</div>

			<v-divider />

			<v-card-text class="flex-grow-1 overflow-y-auto pa-0">
				<div v-if="hasCoordinates" class="detail-map-container ma-4">
					<l-map
						:center="mapCenter"
						:options="{ zoomControl: true, attributionControl: false }"
						:use-global-leaflet="false"
						:zoom="15"
					>
						<l-tile-layer layer-type="base" url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
						<l-marker :lat-lng="mapCenter">
							<l-icon class-name="custom-marker-icon" :icon-anchor="[14, 14]" :icon-size="[28, 28]">
								<div class="detail-marker" />
							</l-icon>
						</l-marker>
					</l-map>
				</div>

				<v-list class="bg-transparent px-2 py-2" density="compact">
					<v-list-subheader>{{ t('score.title') }}</v-list-subheader>

					<v-list-item>
						<ProspectScoreCard :score="prospect.score" />
					</v-list-item>

					<v-divider class="my-2" />

					<v-list-subheader>{{ t('business.title') }}</v-list-subheader>

					<v-list-item>
						<BusinessProfileCard
							:google-editorial-summary="prospect.googleEditorialSummary"
							:profile="prospect.businessProfile"
						/>
					</v-list-item>

					<v-divider class="my-2" />

					<v-list-subheader>{{ t('size.title') }}</v-list-subheader>

					<v-list-item>
						<SizeEstimateCard :estimate="prospect.sizeEstimate" />
					</v-list-item>

					<v-divider class="my-2" />

					<v-list-subheader>{{ t('signals.title') }}</v-list-subheader>

					<v-list-item>
						<SignalsList :signals="prospect.signals ?? []" />
						<v-btn
							v-if="!readonly"
							class="mt-3"
							color="primary"
							:loading="isEnriching"
							prepend-icon="mdi-auto-fix"
							size="small"
							variant="tonal"
							@click="emit('enrich', prospect.id)"
						>
							{{ t('signals.enrich') }}
						</v-btn>
					</v-list-item>

					<v-divider class="my-2" />

					<v-list-subheader>{{ t('analysis.title') }}</v-list-subheader>

					<v-list-item>
						<ProspectAnalysisCard :analysis="prospect.analysis" />
					</v-list-item>

					<v-divider class="my-2" />

					<v-list-subheader>{{ t('sheet.title') }}</v-list-subheader>

					<v-list-item>
						<ProspectSheetCard :sheet="prospect.profileSheet" />
					</v-list-item>

					<v-divider class="my-2" />

					<v-list-subheader>{{ t('messages.title') }}</v-list-subheader>

					<v-list-item>
						<ProspectMessagesCard
							:is-creating="isCreatingMessage"
							:is-deleting="isDeletingMessage"
							:is-marking-sent="isMarkingSent"
							:loading="isFetchingMessages"
							:messages="messages"
							:readonly="readonly"
							@add-message="handleAddMessage"
							@delete="handleDeleteMessage"
							@mark-sent="handleMarkSent"
						/>
					</v-list-item>

					<v-divider class="my-2" />

					<v-list-subheader>{{ t('prospects.details.sections.contact') }}</v-list-subheader>

					<v-list-item prepend-icon="mdi-phone-outline" :title="prospect.phone || t('prospects.details.empty')">
						<template #append>
							<v-btn
								v-if="prospect.phone"
								icon="mdi-content-copy"
								size="small"
								variant="text"
								@click.stop="copyText(prospect.phone)"
							/>
						</template>
					</v-list-item>

					<v-list-item prepend-icon="mdi-email-outline" :title="prospect.email || t('prospects.details.empty')">
						<template #append>
							<v-btn
								v-if="prospect.email"
								icon="mdi-content-copy"
								size="small"
								variant="text"
								@click.stop="copyText(prospect.email)"
							/>
						</template>
					</v-list-item>

					<v-divider class="my-2" />

					<v-list-subheader>{{ t('prospects.details.sections.address') }}</v-list-subheader>

					<v-list-item prepend-icon="mdi-map-marker-outline" :title="prospect.address || t('prospects.details.empty')">
						<template #append>
							<v-btn v-if="prospect.address" icon="mdi-map" size="small" variant="text" @click.stop="openMaps" />
							<v-btn
								v-if="prospect.address"
								icon="mdi-content-copy"
								size="small"
								variant="text"
								@click.stop="copyText(prospect.address)"
							/>
						</template>
					</v-list-item>

					<v-divider class="my-2" />

					<v-list-subheader>{{ t('prospects.details.sections.links') }}</v-list-subheader>

					<v-list-item
						:disabled="!prospect.website"
						prepend-icon="mdi-web"
						:title="prospect.website || t('prospects.details.empty')"
						@click="prospect.website ? openWebsite() : undefined"
					>
						<template #append>
							<v-btn
								v-if="prospect.website"
								icon="mdi-content-copy"
								size="small"
								variant="text"
								@click.stop="copyText(prospect.website)"
							/>
						</template>
					</v-list-item>

					<v-list-item
						:disabled="!prospect.socialLinks?.instagram"
						prepend-icon="mdi-instagram"
						:title="prospect.socialLinks?.instagram || t('prospects.details.empty')"
						@click="prospect.socialLinks?.instagram ? openUrl(prospect.socialLinks.instagram) : undefined"
					>
						<template #append>
							<v-btn
								v-if="prospect.socialLinks?.instagram"
								icon="mdi-content-copy"
								size="small"
								variant="text"
								@click.stop="copyText(prospect.socialLinks.instagram)"
							/>
						</template>
					</v-list-item>

					<v-list-item
						:disabled="!prospect.socialLinks?.facebook"
						prepend-icon="mdi-facebook"
						:title="prospect.socialLinks?.facebook || t('prospects.details.empty')"
						@click="prospect.socialLinks?.facebook ? openUrl(prospect.socialLinks.facebook) : undefined"
					>
						<template #append>
							<v-btn
								v-if="prospect.socialLinks?.facebook"
								icon="mdi-content-copy"
								size="small"
								variant="text"
								@click.stop="copyText(prospect.socialLinks.facebook)"
							/>
						</template>
					</v-list-item>

					<v-list-item
						:disabled="!prospect.socialLinks?.linkedin"
						prepend-icon="mdi-linkedin"
						:title="prospect.socialLinks?.linkedin || t('prospects.details.empty')"
						@click="prospect.socialLinks?.linkedin ? openUrl(prospect.socialLinks.linkedin) : undefined"
					>
						<template #append>
							<v-btn
								v-if="prospect.socialLinks?.linkedin"
								icon="mdi-content-copy"
								size="small"
								variant="text"
								@click.stop="copyText(prospect.socialLinks.linkedin)"
							/>
						</template>
					</v-list-item>

					<v-divider class="my-2" />

					<v-list-subheader>{{ t('prospects.details.sections.notes') }}</v-list-subheader>
					<v-list-item>
						<div class="text-body-2 notes-text">
							{{ prospect.notes || t('prospects.details.no-notes') }}
						</div>
					</v-list-item>
				</v-list>
			</v-card-text>
		</v-card>

		<v-dialog v-model="confirmDelete" max-width="420">
			<v-card elevation="0">
				<v-card-title class="text-h6">{{ t('prospects.details.delete.title') }}</v-card-title>
				<v-card-text>{{ t('prospects.details.delete.description') }}</v-card-text>
				<v-card-actions>
					<v-spacer />
					<v-btn variant="text" @click="confirmDelete = false">{{ t('common.cancel') }}</v-btn>
					<v-btn color="error" :loading="isDeleting" variant="flat" @click="handleDelete">
						{{ t('prospects.details.actions.delete') }}
					</v-btn>
				</v-card-actions>
			</v-card>
		</v-dialog>
	</v-navigation-drawer>
</template>

<style scoped>
.notes-text {
	white-space: pre-wrap;
	line-height: 1.5;
}

.detail-map-container {
	height: 180px;
	border-radius: 12px;
	overflow: hidden;
}

.detail-map-container :deep(.leaflet-container) {
	width: 100%;
	height: 100%;
	border-radius: 12px;
}

.detail-marker {
	width: 28px;
	height: 28px;
	border-radius: 50%;
	background: rgb(var(--v-theme-primary));
	border: 3px solid white;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}
</style>

<script setup lang="ts">
import { LIcon, LMap, LMarker, LTileLayer } from '@vue-leaflet/vue-leaflet';
import { computed, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useDisplay } from 'vuetify';
import { toast } from 'vuetify-sonner';

import type { CreateProspectMessageDto } from '~/api/dtos/prospect-message.dto';
import type { ProspectDto } from '~/api/dtos/prospect.dto';
import BusinessProfileCard from '~/components/business/BusinessProfileCard.vue';
import ProspectAnalysisCard from '~/components/business/ProspectAnalysisCard.vue';
import ProspectMessagesCard from '~/components/prospects/ProspectMessagesCard.vue';
import ProspectSheetCard from '~/components/prospects/ProspectSheetCard.vue';
import ProspectScoreCard from '~/components/signals/ProspectScoreCard.vue';
import SignalsList from '~/components/signals/SignalsList.vue';
import SizeEstimateCard from '~/components/signals/SizeEstimateCard.vue';
import { useProspectMessages } from '~/composables/useProspectMessages';

import 'leaflet/dist/leaflet.css';

interface Props {
	prospect: ProspectDto | null;
	isDeleting?: boolean;
	isEnriching?: boolean;
	readonly?: boolean;
}

interface Emits {
	(e: 'edit'): void;
	(e: 'delete', id: string): void;
	(e: 'enrich', id: string): void;
}

const props = withDefaults(defineProps<Props>(), {
	isDeleting: false,
	isEnriching: false,
	readonly: false
});
const emit = defineEmits<Emits>();
const modelValue = defineModel<boolean>();
const { t } = useI18n();
const { mobile, width } = useDisplay();

const confirmDelete = ref(false);

const {
	messages,
	isFetchingMessages,
	isCreatingMessage,
	isMarkingSent,
	isDeletingMessage,
	fetchMessages,
	addMessage,
	markSent,
	removeMessage,
	clearMessages
} = useProspectMessages();

// The drawer stays mounted: fetch on open and on prospect change, not on mount
watch([() => props.prospect?.id, modelValue], ([prospectId, isOpen]) => {
	clearMessages();
	if (prospectId && isOpen) {
		fetchMessages(prospectId);
	}
});

const handleMarkSent = (messageId: string) => {
	if (!props.prospect) return;
	markSent(props.prospect.id, messageId);
};

const handleAddMessage = (data: CreateProspectMessageDto) => {
	if (!props.prospect) return;
	addMessage(props.prospect.id, data);
};

const handleDeleteMessage = (messageId: string) => {
	if (!props.prospect) return;
	removeMessage(props.prospect.id, messageId);
};

const computedWidth = computed(() => {
	if (mobile.value) return width.value;
	return width.value * 0.35;
});

const hasCoordinates = computed(() => typeof props.prospect?.lat === 'number' && typeof props.prospect?.lng === 'number');

const mapCenter = computed<[number, number]>(() => [(props.prospect?.lat as number) ?? 0, (props.prospect?.lng as number) ?? 0]);

const normalizeUrl = (raw: string) => {
	const trimmed = raw.trim();
	if (!trimmed) return null;
	if (trimmed.startsWith('http://') || trimmed.startsWith('https://')) return trimmed;
	return `https://${trimmed}`;
};

const openUrl = (raw: string) => {
	const url = normalizeUrl(raw);
	if (!url) return;
	window.open(url, '_blank', 'noopener,noreferrer');
};

const openWebsite = () => {
	if (!props.prospect?.website) return;
	openUrl(props.prospect.website);
};

const openMaps = () => {
	const address = props.prospect?.address?.trim();
	if (!address) return;
	const url = `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(address)}`;
	window.open(url, '_blank', 'noopener,noreferrer');
};

const copyText = async (value: string) => {
	try {
		await navigator.clipboard.writeText(value);
		toast.success(t('prospects.details.copy-success'));
	} catch {
		toast.error(t('errors.clipboard-failed'));
	}
};

const handleDelete = () => {
	if (!props.prospect) return;
	emit('delete', props.prospect.id);
	confirmDelete.value = false;
	modelValue.value = false;
};
</script>
