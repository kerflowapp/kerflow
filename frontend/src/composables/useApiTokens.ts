import type { Ref } from 'vue';
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { toast } from '~/composables/useToast';

import { createApiToken$, getApiTokens$, revokeApiToken$ } from '~/api/api-tokens.api';
import type { ApiTokenDto, CreatedApiTokenDto } from '~/api/dtos/api-token.dto';

import { useTrigger } from './useTrigger';

export function useApiTokens(): {
	tokens: Ref<ApiTokenDto[]>;
	isFetching: Ref<boolean>;
	isCreating: Ref<boolean>;
	isRevoking: Ref<boolean>;
	fetchTokens: () => void;
	createToken: (name: string, onCreated: (created: CreatedApiTokenDto) => void) => void;
	revokeToken: (id: string, onRevoked?: () => void) => void;
} {
	const { t } = useI18n();
	const tokens = ref<ApiTokenDto[]>([]);

	const { trigger: triggerFetch, loading: isFetching } = useTrigger();
	const { trigger: triggerCreate, loading: isCreating } = useTrigger();
	const { trigger: triggerRevoke, loading: isRevoking } = useTrigger();

	const fetchTokens = () => {
		triggerFetch(getApiTokens$(), {
			onSuccess: (response: { data: ApiTokenDto[] }) => {
				tokens.value = response.data;
			},
			onError: () => {
				toast.error(t('errors.fetch-api-tokens-failed'));
			}
		});
	};

	const createToken = (name: string, onCreated: (created: CreatedApiTokenDto) => void) => {
		triggerCreate(createApiToken$(name), {
			onSuccess: (response: { data: CreatedApiTokenDto }) => {
				toast.success(t('success.api-token-created'));
				onCreated(response.data);
				fetchTokens();
			},
			onError: () => {
				toast.error(t('errors.create-api-token-failed'));
			}
		});
	};

	const revokeToken = (id: string, onRevoked?: () => void) => {
		triggerRevoke(revokeApiToken$(id), {
			onSuccess: () => {
				toast.success(t('success.api-token-revoked'));
				onRevoked?.();
				fetchTokens();
			},
			onError: () => {
				toast.error(t('errors.revoke-api-token-failed'));
			}
		});
	};

	return {
		tokens,
		isFetching,
		isCreating,
		isRevoking,
		fetchTokens,
		createToken,
		revokeToken
	};
}
