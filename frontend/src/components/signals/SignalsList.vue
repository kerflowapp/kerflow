<template>
	<div v-if="sortedSignals.length" class="d-flex flex-wrap ga-2">
		<v-tooltip v-for="signal in sortedSignals" :key="signal.type" location="top" max-width="320">
			<template #activator="{ props: tipProps }">
				<v-chip v-bind="tipProps" :color="importanceColor(signal.importance)" size="small" variant="tonal">
					<v-icon size="14" start>{{ signalIcon(signal.type) }}</v-icon>
					{{ t(`signals.types.${signal.type}.label`) }}
				</v-chip>
			</template>
			<span>{{ t(`signals.types.${signal.type}.explanation`, signalParams(signal)) }}</span>
		</v-tooltip>
	</div>
	<div v-else class="text-body-2 text-medium-emphasis">{{ t('signals.empty') }}</div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

import type { SignalDto, SignalImportance } from '~/api/dtos/prospect.dto';

interface Props {
	signals: SignalDto[];
}

const props = defineProps<Props>();
const { t } = useI18n();

const IMPORTANCE_ORDER: Record<SignalImportance, number> = {
	HIGH: 0,
	MEDIUM: 1,
	LOW: 2
};

const SIGNAL_ICONS: Record<string, string> = {
	MANY_REVIEWS: 'mdi-star',
	MOBILE_ONLY_PHONE: 'mdi-cellphone',
	NO_WEBSITE: 'mdi-web-off',
	NO_HTTPS: 'mdi-lock-open-outline',
	WORDPRESS: 'mdi-wordpress',
	PRO_EMAIL: 'mdi-email-check-outline',
	GENERIC_EMAIL: 'mdi-email-outline',
	RECRUITING_DETECTED: 'mdi-account-search-outline',
	FLEET_DETECTED: 'mdi-truck-outline',
	PROFESSIONAL_WEBSITE: 'mdi-web-check',
	ACTIVE_GOOGLE_PRESENCE: 'mdi-google',
	SOCIAL_MEDIA_PRESENCE: 'mdi-account-group-outline',
	MULTI_ESTABLISHMENT: 'mdi-office-building-marker',
	COMPANY_MATURE: 'mdi-calendar-check'
};

const sortedSignals = computed(() =>
	[...props.signals].sort((a, b) => (IMPORTANCE_ORDER[a.importance] ?? 3) - (IMPORTANCE_ORDER[b.importance] ?? 3))
);

const importanceColor = (importance: SignalImportance): string => {
	if (importance === 'HIGH') return 'success';
	if (importance === 'MEDIUM') return 'warning';
	return 'grey';
};

const signalIcon = (type: string): string => SIGNAL_ICONS[type] ?? 'mdi-information-outline';

const signalParams = (signal: SignalDto): Record<string, unknown> => signal.params ?? {};
</script>
