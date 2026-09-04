import type { Ref } from 'vue';
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { toast } from '~/composables/useToast';

import type { CreateProspectMessageDto, ProspectMessageDto } from '~/api/dtos/prospect-message.dto';
import {
	createProspectMessage$,
	deleteProspectMessage$,
	getProspectMessages$,
	markProspectMessageSent$
} from '~/api/prospect-messages.api';

import { useTrigger } from './useTrigger';

/**
 * Outreach thread of one prospect. State is local (not in the Pinia store):
 * it is drawer-scoped and lazily fetched when the drawer opens.
 */
export function useProspectMessages(): {
	messages: Ref<ProspectMessageDto[]>;
	isFetchingMessages: Ref<boolean>;
	isCreatingMessage: Ref<boolean>;
	isMarkingSent: Ref<boolean>;
	isDeletingMessage: Ref<boolean>;
	fetchMessages: (prospectId: string) => void;
	addMessage: (prospectId: string, data: CreateProspectMessageDto, onAdded?: () => void) => void;
	markSent: (prospectId: string, messageId: string) => void;
	removeMessage: (prospectId: string, messageId: string) => void;
	clearMessages: () => void;
} {
	const { t } = useI18n();
	const messages = ref<ProspectMessageDto[]>([]);

	const { trigger: triggerFetch, loading: isFetchingMessages } = useTrigger();
	const { trigger: triggerCreate, loading: isCreatingMessage } = useTrigger();
	const { trigger: triggerMarkSent, loading: isMarkingSent } = useTrigger();
	const { trigger: triggerDelete, loading: isDeletingMessage } = useTrigger();

	const fetchMessages = (prospectId: string) => {
		triggerFetch(getProspectMessages$(prospectId), {
			onSuccess: (response: { data: ProspectMessageDto[] }) => {
				messages.value = response.data;
			},
			onError: () => {
				toast.error(t('errors.fetch-messages-failed'));
			}
		});
	};

	const addMessage = (prospectId: string, data: CreateProspectMessageDto, onAdded?: () => void) => {
		triggerCreate(createProspectMessage$(prospectId, data), {
			onSuccess: (response: { data: ProspectMessageDto }) => {
				messages.value.push(response.data);
				toast.success(t('success.message-recorded'));
				onAdded?.();
			},
			onError: () => {
				toast.error(t('errors.create-message-failed'));
			}
		});
	};

	const markSent = (prospectId: string, messageId: string) => {
		triggerMarkSent(markProspectMessageSent$(prospectId, messageId), {
			onSuccess: (response: { data: ProspectMessageDto }) => {
				messages.value = messages.value.map(message => (message.id === messageId ? response.data : message));
				toast.success(t('success.message-marked-sent'));
			},
			onError: () => {
				toast.error(t('errors.mark-sent-failed'));
			}
		});
	};

	const removeMessage = (prospectId: string, messageId: string) => {
		triggerDelete(deleteProspectMessage$(prospectId, messageId), {
			onSuccess: () => {
				messages.value = messages.value.filter(message => message.id !== messageId);
				toast.success(t('success.message-deleted'));
			},
			onError: () => {
				toast.error(t('errors.delete-message-failed'));
			}
		});
	};

	const clearMessages = () => {
		messages.value = [];
	};

	return {
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
	};
}
