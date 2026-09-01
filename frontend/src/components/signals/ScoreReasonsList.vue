<template>
	<div class="d-flex flex-column ga-1">
		<div v-for="reason in reasons" :key="reason.type" class="d-flex align-center justify-space-between ga-4">
			<span class="text-body-2">{{ t(`signals.types.${reason.type}.label`) }}</span>
			<span class="text-body-2 font-weight-bold" :class="reason.points > 0 ? 'text-success' : 'text-error'">
				{{ reason.points > 0 ? '+' : '' }}{{ reason.points }}
			</span>
		</div>
	</div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n';

import type { ScoreReasonDto } from '~/api/dtos/prospect.dto';

interface Props {
	reasons: ScoreReasonDto[];
}

defineProps<Props>();
const { t } = useI18n();
</script>
