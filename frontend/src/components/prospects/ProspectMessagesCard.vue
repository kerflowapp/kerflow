<template>
	<div>
		<v-progress-circular v-if="loading" class="my-2" indeterminate size="24" />

		<div v-else-if="!messages.length" class="text-body-2 text-medium-emphasis">
			{{ t('messages.empty') }}
		</div>

		<template v-else>
			<v-card v-for="message in messages" :key="message.id" class="mb-3" elevation="0" variant="outlined">
				<v-card-text class="pa-3">
					<div class="d-flex align-center ga-2 mb-2">
						<v-icon :icon="channelIcon(message.channel)" size="small" />
						<v-chip :color="chipColor(message)" size="x-small" variant="tonal">
							{{ chipLabel(message) }}
						</v-chip>
						<span class="text-caption text-medium-emphasis">
							{{ t(channelLabelKey(message.channel)) }} · {{ messageDate(message) }}
						</span>
						<v-spacer />
						<v-btn icon="mdi-content-copy" size="x-small" variant="text" @click="copyMessage(message)" />
						<v-btn
							v-if="!readonly"
							color="error"
							icon="mdi-delete-outline"
							size="x-small"
							variant="text"
							@click="confirmDeleteId = message.id"
						/>
					</div>

					<div v-if="message.subject" class="text-body-2 font-weight-bold mb-1">
						{{ message.subject }}
					</div>

					<div
						class="text-body-2 message-body"
						:class="{ 'message-body--collapsed': !expanded.has(message.id) && isLong(message) }"
					>
						{{ message.body }}
					</div>

					<v-btn
						v-if="isLong(message)"
						class="mt-1 px-0"
						color="primary"
						size="x-small"
						variant="text"
						@click="toggleExpanded(message.id)"
					>
						{{ expanded.has(message.id) ? t('messages.show-less') : t('messages.show-more') }}
					</v-btn>

					<div v-if="message.generatedBy" class="text-caption text-medium-emphasis mt-2">
						{{ t('messages.generated-by', { by: message.generatedBy }) }}
					</div>

					<v-btn
						v-if="!readonly && message.direction === 'OUTBOUND' && message.status === 'DRAFT'"
						class="mt-2"
						color="success"
						:loading="isMarkingSent"
						prepend-icon="mdi-email-check-outline"
						size="small"
						variant="tonal"
						@click="emit('mark-sent', message.id)"
					>
						{{ t('messages.mark-sent') }}
					</v-btn>
				</v-card-text>
			</v-card>
		</template>

		<v-btn v-if="!readonly" color="primary" prepend-icon="mdi-plus" size="small" variant="tonal" @click="messageDialog = true">
			{{ t('messages.add-exchange') }}
		</v-btn>

		<ProspectMessageDialog v-model="messageDialog" :loading="isCreating" @submit="emit('add-message', $event)" />

		<v-dialog max-width="420" :model-value="!!confirmDeleteId" @update:model-value="confirmDeleteId = null">
			<v-card elevation="0">
				<v-card-title class="text-h6">{{ t('messages.delete.title') }}</v-card-title>
				<v-card-text>{{ t('messages.delete.description') }}</v-card-text>
				<v-card-actions>
					<v-spacer />
					<v-btn variant="text" @click="confirmDeleteId = null">{{ t('common.cancel') }}</v-btn>
					<v-btn color="error" :loading="isDeleting" variant="flat" @click="handleDelete">
						{{ t('common.delete') }}
					</v-btn>
				</v-card-actions>
			</v-card>
		</v-dialog>
	</div>
</template>

<style scoped>
.message-body {
	white-space: pre-wrap;
	line-height: 1.5;
}

.message-body--collapsed {
	max-height: 120px;
	overflow: hidden;
}
</style>

<script setup lang="ts">
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { toast } from 'vuetify-sonner';

import type { CreateProspectMessageDto, ProspectMessageDto } from '~/api/dtos/prospect-message.dto';
import ProspectMessageDialog from '~/components/prospects/ProspectMessageDialog.vue';
import { channelIcon, channelLabelKey } from '~/utils/message-channel';

const LONG_BODY_LENGTH = 400;

interface Props {
	messages: ProspectMessageDto[];
	loading?: boolean;
	isCreating?: boolean;
	isMarkingSent?: boolean;
	isDeleting?: boolean;
	readonly?: boolean;
}

interface Emits {
	(e: 'mark-sent', messageId: string): void;
	(e: 'add-message', data: CreateProspectMessageDto): void;
	(e: 'delete', messageId: string): void;
}

withDefaults(defineProps<Props>(), {
	loading: false,
	isCreating: false,
	isMarkingSent: false,
	isDeleting: false,
	readonly: false
});
const emit = defineEmits<Emits>();
const { t, locale } = useI18n();

const expanded = ref(new Set<string>());
const messageDialog = ref(false);
const confirmDeleteId = ref<string | null>(null);

const chipColor = (message: ProspectMessageDto) => {
	if (message.direction === 'INBOUND') return 'info';
	return message.status === 'SENT' ? 'success' : 'warning';
};

const chipLabel = (message: ProspectMessageDto) => {
	if (message.direction === 'INBOUND') return t('messages.status.reply');
	return message.status === 'SENT' ? t('messages.status.sent') : t('messages.status.draft');
};

const messageDate = (message: ProspectMessageDto) => {
	const raw = message.sentAt ?? message.receivedAt ?? message.creationDate;
	if (!raw) return '';
	const date = new Date(raw);
	return Number.isNaN(date.getTime()) ? '' : date.toLocaleDateString(locale.value);
};

const isLong = (message: ProspectMessageDto) => message.body.length > LONG_BODY_LENGTH;

const toggleExpanded = (id: string) => {
	const next = new Set(expanded.value);
	if (next.has(id)) {
		next.delete(id);
	} else {
		next.add(id);
	}
	expanded.value = next;
};

const copyMessage = async (message: ProspectMessageDto) => {
	const text = message.subject ? `${message.subject}\n\n${message.body}` : message.body;
	try {
		await navigator.clipboard.writeText(text);
		toast.success(t('prospects.details.copy-success'));
	} catch {
		toast.error(t('errors.clipboard-failed'));
	}
};

const handleDelete = () => {
	if (!confirmDeleteId.value) return;
	emit('delete', confirmDeleteId.value);
	confirmDeleteId.value = null;
};
</script>
