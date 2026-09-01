<template>
	<div v-if="score">
		<div class="d-flex align-center ga-2">
			<v-rating active-color="warning" color="warning" density="compact" :model-value="score.stars" readonly size="small" />
			<span class="text-body-2 text-medium-emphasis">{{ score.score }}/100</span>
		</div>
		<template v-if="score.reasons.length">
			<div class="text-subtitle-2 font-weight-bold mt-2 mb-1">{{ t('score.why') }}</div>
			<ScoreReasonsList :reasons="score.reasons" />
		</template>
	</div>
	<div v-else class="text-body-2 text-medium-emphasis">{{ t('score.empty') }}</div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n';

import type { ProspectScoreDto } from '~/api/dtos/prospect.dto';
import ScoreReasonsList from '~/components/signals/ScoreReasonsList.vue';

interface Props {
	score?: ProspectScoreDto | null;
}

defineProps<Props>();
const { t } = useI18n();
</script>
