import type { Ref } from 'vue';
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { toast } from '~/composables/useToast';

import type { AuthorizationRequestDto, ConsentDecisionDto } from '~/api/dtos/oauth.dto';
import { approveAuthorizationRequest$, denyAuthorizationRequest$, getAuthorizationRequest$ } from '~/api/oauth.api';

import { useTrigger } from './useTrigger';

export function useOAuthConsent(): {
	request: Ref<AuthorizationRequestDto | null>;
	isExpired: Ref<boolean>;
	isFetching: Ref<boolean>;
	isDeciding: Ref<boolean>;
	fetchRequest: (requestId: string) => void;
	approve: (requestId: string) => void;
	deny: (requestId: string) => void;
} {
	const { t } = useI18n();
	const request = ref<AuthorizationRequestDto | null>(null);
	const isExpired = ref(false);

	const { trigger: triggerFetch, loading: isFetching } = useTrigger();
	const { trigger: triggerDecide, loading: isDeciding } = useTrigger();

	// The callback belongs to the OAuth client (claude.ai), so this leaves the SPA entirely:
	// a router push would try to resolve it as an app route.
	const leaveTo = (redirectUri: string) => {
		window.location.href = redirectUri;
	};

	const fetchRequest = (requestId: string) => {
		triggerFetch(getAuthorizationRequest$(requestId), {
			onSuccess: (response: { data: AuthorizationRequestDto }) => {
				request.value = response.data;
			},
			onError: () => {
				isExpired.value = true;
			}
		});
	};

	const approve = (requestId: string) => {
		triggerDecide(approveAuthorizationRequest$(requestId), {
			onSuccess: (response: { data: ConsentDecisionDto }) => {
				leaveTo(response.data.redirectUri);
			},
			onError: () => {
				toast.error(t('errors.oauth-consent-failed'));
			}
		});
	};

	const deny = (requestId: string) => {
		triggerDecide(denyAuthorizationRequest$(requestId), {
			onSuccess: (response: { data: ConsentDecisionDto }) => {
				leaveTo(response.data.redirectUri);
			},
			onError: () => {
				toast.error(t('errors.oauth-consent-failed'));
			}
		});
	};

	return {
		request,
		isExpired,
		isFetching,
		isDeciding,
		fetchRequest,
		approve,
		deny
	};
}
