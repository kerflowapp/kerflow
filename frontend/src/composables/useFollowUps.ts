import type { ComputedRef } from 'vue';
import { computed } from 'vue';

import type { ProspectDto } from '~/api/dtos/prospect.dto';
import { useAuthStore } from '~/stores/auth.store';
import { useProspectsStore } from '~/stores/prospects.store';
import type { FollowUpState } from '~/utils/follow-up';
import { DEFAULT_FOLLOW_UP_DELAY_DAYS, followUpStateOf } from '~/utils/follow-up';

/** Prospects waiting for a reply past the user's follow-up delay. */
export function useFollowUps(): {
	delayDays: ComputedRef<number>;
	followUpState: (prospect: ProspectDto) => FollowUpState;
	isDue: (prospect: ProspectDto) => boolean;
	dueProspects: ComputedRef<ProspectDto[]>;
	dueCount: ComputedRef<number>;
} {
	const authStore = useAuthStore();
	const store = useProspectsStore();

	const delayDays = computed(() => authStore.user?.followUpDelayDays ?? DEFAULT_FOLLOW_UP_DELAY_DAYS);

	const followUpState = (prospect: ProspectDto) => followUpStateOf(prospect, delayDays.value);
	const isDue = (prospect: ProspectDto) => followUpState(prospect).status === 'due';

	const dueProspects = computed(() => store.prospects.filter(isDue));
	const dueCount = computed(() => dueProspects.value.length);

	return { delayDays, followUpState, isDue, dueProspects, dueCount };
}
