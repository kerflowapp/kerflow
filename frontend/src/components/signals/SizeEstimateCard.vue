<template>
	<div v-if="estimate">
		<div class="d-flex flex-wrap align-center ga-2">
			<v-chip color="primary" size="small" variant="tonal">
				<v-icon size="14" start>mdi-account-group</v-icon>
				{{ t(`size.buckets.${estimate.bucket}`) }}
			</v-chip>
			<v-tooltip location="top" max-width="320">
				<template #activator="{ props: tipProps }">
					<v-chip v-bind="tipProps" :color="confidenceColor(estimate.confidence)" size="x-small" variant="tonal">
						{{ t(`size.confidence.${estimate.confidence}`) }}
					</v-chip>
				</template>
				<span>{{ t(`size.confidence-explanation.${estimate.confidence}`) }}</span>
			</v-tooltip>
		</div>
		<ul v-if="visibleReasons.length" class="text-body-2 text-medium-emphasis mt-2 ps-4">
			<li v-for="reason in visibleReasons" :key="reason.key">{{ reasonText(reason) }}</li>
		</ul>
	</div>
	<div v-else class="text-body-2 text-medium-emphasis">{{ t('size.unknown') }}</div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

import type { SizeConfidence, SizeEstimateDto, SizeReasonDto } from '~/api/dtos/prospect.dto';

interface Props {
	estimate?: SizeEstimateDto | null;
}

const props = defineProps<Props>();
const { t, te } = useI18n();

const confidenceColor = (confidence: SizeConfidence): string => {
	if (confidence === 'HIGH') return 'success';
	if (confidence === 'MEDIUM') return 'warning';
	return 'grey';
};

// A reason key the backend added before this app knows it renders nothing, never a raw key.
const visibleReasons = computed(() => (props.estimate?.reasons ?? []).filter(reason => te(`size.reasons.${reason.key}`)));

const reasonText = (reason: SizeReasonDto): string => t(`size.reasons.${reason.key}`, { ...(reason.params ?? {}) });
</script>
