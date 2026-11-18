<template>
	<v-navigation-drawer v-model="modelValue" location="right" temporary :width="computedWidth">
		<v-card v-if="prospect" class="h-100 d-flex flex-column" elevation="0">
			<v-card-title class="d-flex align-start pa-2">
				<div class="flex-grow-1" style="min-width: 0">
					<InlineEditableField
						class="text-h6 font-weight-bold"
						:empty-text="t('prospects.details.empty')"
						:label="t('prospects.fields.name')"
						:loading="isUpdating"
						:model-value="prospect.name"
						:readonly="readonly"
						required
						@save="emitUpdate({ name: $event })"
					/>
					<div v-if="prospect.email" class="px-4 text-caption text-medium-emphasis text-truncate">
						{{ prospect.email }}
					</div>
				</div>
				<div class="d-flex align-center ga-1 flex-shrink-0 pt-2">
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

			<div class="px-4 pb-3">
				<ProspectTagsField
					:loading="isUpdating"
					:model-value="prospect.tags ?? []"
					:readonly="readonly"
					@save="emitUpdate({ tags: $event })"
				/>
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
							:is-updating="isUpdatingMessage"
							:loading="isFetchingMessages"
							:messages="messages"
							:readonly="readonly"
							@add-message="handleAddMessage"
							@delete="handleDeleteMessage"
							@mark-sent="handleMarkSent"
							@update-message="handleUpdateMessage"
						/>
					</v-list-item>

					<v-divider class="my-2" />

					<v-list-subheader>{{ t('prospects.details.sections.follow-up') }}</v-list-subheader>
					<div class="px-4 py-2">
						<ProspectFollowUpDateField
							:disabled="readonly"
							:loading="isUpdatingFollowUp"
							:model-value="prospect.followUpDate"
							@update="emit('update-follow-up-date', prospect!.id, $event)"
						/>
					</div>

					<v-divider class="my-2" />

					<ProspectContactSection
						:loading="isUpdating"
						:prospect="prospect"
						:readonly="readonly"
						@update="emitUpdate"
					/>
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

import type { CreateProspectMessageDto, UpdateProspectMessageDto } from '~/api/dtos/prospect-message.dto';
import type { ProspectDto, UpdateProspectDto } from '~/api/dtos/prospect.dto';
import BusinessProfileCard from '~/components/business/BusinessProfileCard.vue';
import ProspectAnalysisCard from '~/components/business/ProspectAnalysisCard.vue';
import InlineEditableField from '~/components/prospects/InlineEditableField.vue';
import ProspectContactSection from '~/components/prospects/ProspectContactSection.vue';
import ProspectFollowUpDateField from '~/components/prospects/ProspectFollowUpDateField.vue';
import ProspectMessagesCard from '~/components/prospects/ProspectMessagesCard.vue';
import ProspectSheetCard from '~/components/prospects/ProspectSheetCard.vue';
import ProspectTagsField from '~/components/prospects/ProspectTagsField.vue';
import ProspectScoreCard from '~/components/signals/ProspectScoreCard.vue';
import SignalsList from '~/components/signals/SignalsList.vue';
import SizeEstimateCard from '~/components/signals/SizeEstimateCard.vue';
import { useProspectMessages } from '~/composables/useProspectMessages';

import 'leaflet/dist/leaflet.css';

interface Props {
	prospect: ProspectDto | null;
	isDeleting?: boolean;
	isEnriching?: boolean;
	isUpdatingFollowUp?: boolean;
	isUpdating?: boolean;
	readonly?: boolean;
}

interface Emits {
	(e: 'update', id: string, data: UpdateProspectDto): void;
	(e: 'delete', id: string): void;
	(e: 'enrich', id: string): void;
	(e: 'update-follow-up-date', id: string, followUpDate: string | null): void;
}

const props = withDefaults(defineProps<Props>(), {
	isDeleting: false,
	isEnriching: false,
	isUpdatingFollowUp: false,
	isUpdating: false,
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
	isUpdatingMessage,
	isMarkingSent,
	isDeletingMessage,
	fetchMessages,
	addMessage,
	updateMessage,
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

const handleUpdateMessage = (messageId: string, data: UpdateProspectMessageDto) => {
	if (!props.prospect) return;
	updateMessage(props.prospect.id, messageId, data);
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

const emitUpdate = (data: UpdateProspectDto) => {
	if (!props.prospect) return;
	emit('update', props.prospect.id, data);
};

const handleDelete = () => {
	if (!props.prospect) return;
	emit('delete', props.prospect.id);
	confirmDelete.value = false;
	modelValue.value = false;
};
</script>
