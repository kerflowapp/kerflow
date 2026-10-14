<template>
	<v-dialog max-width="560" :model-value="modelValue" @update:model-value="close">
		<v-card elevation="0">
			<v-card-title class="text-h6">{{ message ? t('messages.dialog.edit-title') : t('messages.dialog.title') }}</v-card-title>
			<v-card-text>
				<v-btn-toggle v-model="direction" class="mb-4" density="comfortable" divided mandatory variant="outlined">
					<v-btn prepend-icon="mdi-arrow-bottom-left" value="INBOUND">
						{{ t('messages.direction.inbound') }}
					</v-btn>
					<v-btn prepend-icon="mdi-arrow-top-right" value="OUTBOUND">
						{{ t('messages.direction.outbound') }}
					</v-btn>
				</v-btn-toggle>

				<v-row dense>
					<v-col cols="12" sm="7">
						<v-select
							v-model="channel"
							density="comfortable"
							:items="channelItems"
							:label="t('messages.dialog.channel')"
							:prepend-inner-icon="channelIcon(channel)"
						/>
					</v-col>
					<v-col cols="12" sm="5">
						<v-text-field v-model="date" density="comfortable" :label="t('messages.dialog.date')" type="date" />
					</v-col>
				</v-row>

				<v-text-field
					v-if="channel === 'EMAIL'"
					v-model="subject"
					class="mb-2"
					density="comfortable"
					:label="t('messages.dialog.subject')"
				/>

				<v-textarea v-model="body" auto-grow :label="t('messages.dialog.body')" rows="6" />

				<v-checkbox
					v-if="direction === 'OUTBOUND'"
					v-model="alreadySent"
					density="compact"
					:hint="t('messages.dialog.already-sent-hint')"
					:label="t('messages.dialog.already-sent')"
					persistent-hint
				/>
			</v-card-text>
			<v-card-actions>
				<v-spacer />
				<v-btn variant="text" @click="close">{{ t('common.cancel') }}</v-btn>
				<v-btn color="primary" :disabled="!body.trim()" :loading="loading" variant="flat" @click="submit">
					{{ t('common.save') }}
				</v-btn>
			</v-card-actions>
		</v-card>
	</v-dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';

import type {
	CreateProspectMessageDto,
	MessageChannel,
	MessageDirection,
	ProspectMessageDto
} from '~/api/dtos/prospect-message.dto';
import { MESSAGE_CHANNELS, channelIcon, channelLabelKey, channelOf } from '~/utils/message-channel';
import { occurredAtOf } from '~/utils/message-date';

interface Props {
	modelValue: boolean;
	loading?: boolean;
	/** Set to edit an existing message; null or omitted to record a new one. */
	message?: ProspectMessageDto | null;
}

interface Emits {
	(e: 'update:modelValue', value: boolean): void;
	(e: 'submit', data: CreateProspectMessageDto): void;
}

const props = withDefaults(defineProps<Props>(), { loading: false, message: null });
const emit = defineEmits<Emits>();
const { t } = useI18n();

// Local calendar date: toISOString() would shift it to UTC.
const toDateInput = (date: Date) =>
	`${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;

const today = () => toDateInput(new Date());

const direction = ref<MessageDirection>('INBOUND');
const channel = ref<MessageChannel>('EMAIL');
const date = ref(today());
const subject = ref('');
const body = ref('');
const alreadySent = ref(true);

const channelItems = computed(() => MESSAGE_CHANNELS.map(value => ({ value, title: t(channelLabelKey(value)) })));

const reset = () => {
	direction.value = 'INBOUND';
	channel.value = 'EMAIL';
	date.value = today();
	subject.value = '';
	body.value = '';
	alreadySent.value = true;
};

const prefill = (message: ProspectMessageDto) => {
	const raw = occurredAtOf(message);
	const parsed = raw ? new Date(raw) : null;

	direction.value = message.direction;
	channel.value = channelOf(message.channel);
	date.value = parsed && !Number.isNaN(parsed.getTime()) ? toDateInput(parsed) : today();
	subject.value = message.subject ?? '';
	body.value = message.body;
	alreadySent.value = message.status === 'SENT';
};

watch(
	() => props.modelValue,
	isOpen => {
		if (isOpen && props.message) prefill(props.message);
	}
);

const close = () => {
	emit('update:modelValue', false);
	reset();
};

const submit = () => {
	const trimmedBody = body.value.trim();
	if (!trimmedBody) return;

	// Noon keeps the date the user picked on their side of any timezone shift.
	const occurredAt = new Date(`${date.value}T12:00:00`).toISOString();

	emit('submit', {
		direction: direction.value,
		channel: channel.value,
		status: direction.value === 'OUTBOUND' ? (alreadySent.value ? 'SENT' : 'DRAFT') : undefined,
		subject: channel.value === 'EMAIL' ? subject.value.trim() || undefined : undefined,
		body: trimmedBody,
		occurredAt
	});
	close();
};
</script>
