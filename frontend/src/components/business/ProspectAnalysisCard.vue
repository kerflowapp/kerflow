<template>
	<div v-if="analysis">
		<p class="text-body-2">{{ analysis.summary }}</p>

		<template v-if="analysis.detectedNeeds?.length">
			<div class="text-caption text-medium-emphasis mt-3">{{ t('analysis.needs') }}</div>
			<ul class="text-body-2 ps-4">
				<li v-for="need in analysis.detectedNeeds" :key="need">{{ need }}</li>
			</ul>
		</template>

		<template v-if="analysis.suggestedApproach">
			<div class="text-caption text-medium-emphasis mt-3">{{ t('analysis.approach') }}</div>
			<p class="text-body-2">{{ analysis.suggestedApproach }}</p>
		</template>

		<div class="text-caption text-medium-emphasis mt-3">
			{{ t('analysis.generated-by', { by: analysis.generatedBy, date: analyzedAt }) }}
		</div>
	</div>
	<div v-else class="text-body-2 text-medium-emphasis">{{ t('analysis.empty') }}</div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

import type { ProspectAnalysisDto } from '~/api/dtos/prospect.dto';

interface Props {
	analysis?: ProspectAnalysisDto | null;
}

const props = defineProps<Props>();
const { t, locale } = useI18n();

const analyzedAt = computed(() => {
	if (!props.analysis?.analyzedAt) return '';
	const date = new Date(props.analysis.analyzedAt);
	return Number.isNaN(date.getTime()) ? '' : date.toLocaleDateString(locale.value);
});
</script>
