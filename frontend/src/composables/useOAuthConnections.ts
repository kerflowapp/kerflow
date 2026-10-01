import type { Ref } from 'vue';
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { toast } from '~/composables/useToast';

import type { OAuthConnectionDto } from '~/api/dtos/oauth-connection.dto';
import { getOAuthConnections$, revokeOAuthConnection$ } from '~/api/oauth-connections.api';

import { useTrigger } from './useTrigger';

export function useOAuthConnections(): {
	connections: Ref<OAuthConnectionDto[]>;
	isFetching: Ref<boolean>;
	isRevoking: Ref<boolean>;
	fetchConnections: () => void;
	revokeConnection: (clientId: string, onRevoked?: () => void) => void;
} {
	const { t } = useI18n();
	const connections = ref<OAuthConnectionDto[]>([]);

	const { trigger: triggerFetch, loading: isFetching } = useTrigger();
	const { trigger: triggerRevoke, loading: isRevoking } = useTrigger();

	const fetchConnections = () => {
		triggerFetch(getOAuthConnections$(), {
			onSuccess: (response: { data: OAuthConnectionDto[] }) => {
				connections.value = response.data;
			},
			onError: () => {
				toast.error(t('errors.fetch-oauth-connections-failed'));
			}
		});
	};

	const revokeConnection = (clientId: string, onRevoked?: () => void) => {
		triggerRevoke(revokeOAuthConnection$(clientId), {
			onSuccess: () => {
				toast.success(t('success.oauth-connection-revoked'));
				onRevoked?.();
				fetchConnections();
			},
			onError: () => {
				toast.error(t('errors.revoke-oauth-connection-failed'));
			}
		});
	};

	return {
		connections,
		isFetching,
		isRevoking,
		fetchConnections,
		revokeConnection
	};
}
