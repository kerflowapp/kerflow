<template>
	<v-tooltip v-if="state.status !== 'none'" location="top">
		<template #activator="{ props: tooltipProps }">
			<v-chip
				v-bind="tooltipProps"
				class="mt-1"
				:color="CHIP_COLORS[state.status]"
				:prepend-icon="state.status === 'upcoming' ? 'mdi-bell-outline' : 'mdi-bell-ring-outline'"
				size="x-small"
				variant="tonal"
			>
				{{ label }}
			</v-chip>
		</template>
		<div>{{ t('follow-ups.waiting-tooltip', state.daysWaiting) }}</div>
		<div>{{ t('follow-ups.planned-on', { date: formattedDueDate }) }}</div>
	</v-tooltip>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

import type { ProspectDto } from '~/api/dtos/prospect.dto';
import { useFollowUps } from '~/composables/useFollowUps';
import type { FollowUpStatus } from '~/utils/follow-up';

interface Props {
	prospect: ProspectDto;
}

const CHIP_COLORS: Record<FollowUpStatus, string | undefined> = {
	none: undefined,
	upcoming: 'grey',
	soon: 'warning',
	due: 'error'
};

const props = defineProps<Props>();
const { t, locale } = useI18n();
const { followUpState } = useFollowUps();

const state = computed(() => followUpState(props.prospect));

const label = computed(() => {
	switch (state.value.status) {
		case 'due':
			return t('follow-ups.due', { days: state.value.daysWaiting });
		case 'soon':
			return t('follow-ups.due-tomorrow');
		default:
			return t('follow-ups.due-in', { days: -state.value.daysOverdue });
	}
});

const formattedDueDate = computed(() =>
	state.value.dueDate?.toLocaleDateString(locale.value, { weekday: 'long', day: 'numeric', month: 'long' })
);
</script>
